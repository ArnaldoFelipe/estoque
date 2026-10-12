# 🏢 Microsserviço de Estoque

## 💼 Inventory Service — Event-Driven & Coreografia Saga

Microsserviço de retaguarda focado na **gestão de catálogo e reservas de produtos**.

O serviço opera no modelo **Event-Driven**, reagindo diretamente aos eventos publicados no broker de mensageria, sem expor portas HTTP públicas desnecessárias.

É responsável pela integridade atômica do saldo de itens no armazém e pela emissão de confirmações ou compensações de negócio.

---

# 👨‍💻 Sobre o Projeto

O microsserviço é responsável por:

- 📨 Consumo estritamente assíncrono via **RabbitMQ** utilizando `@RabbitListener`
- 🔎 Validação atômica da disponibilidade de estoque
- 💾 Atualização transacional utilizando **Spring Data JPA** e **PostgreSQL**
- 📤 Emissão do evento de aprovação `PedidoAprovadoEvent` através do `RabbitTemplate`
- ⚠️ Lançamento de exceções customizadas para produtos inexistentes ou saldo insuficiente
- 🔒 Isolamento total de domínio, sem acesso ou conhecimento do banco de dados de pedidos
- 🔄 Participação na transação distribuída através do padrão **Saga por Coreografia**

---

# 🏗️ Fluxo Transacional do Estoque

O processamento de uma solicitação de baixa de estoque ocorre de forma assíncrona através do RabbitMQ.

```text
┌──────────────────┐
│  Order Service   │
└────────┬─────────┘
         │
         │ BaixaEstoqueEvent
         ▼
┌──────────────────┐
│     RabbitMQ     │
└────────┬─────────┘
         │
         │ estoque.baixar.queue
         ▼
┌──────────────────┐
│ Estoque Service  │
└────────┬─────────┘
         │
         ├── Busca produto
         │
         ├── Valida estoque
         │
         ├── Realiza baixa
         │
         └── Persiste alteração
         │
         ▼
┌──────────────────┐
│    PostgreSQL    │
└──────────────────┘
         │
         │ Operação aprovada
         ▼
┌──────────────────┐
│     RabbitMQ     │
└────────┬─────────┘
         │
         │ pedido.aprovar.rk
         ▼
┌──────────────────┐
│  Order Service   │
└──────────────────┘
```

### 🔄 Etapas do processamento

#### 1. Recepção

A classe `EstoqueListener` consome a mensagem enviada para a fila:

```text
estoque.baixar.queue
```

O evento recebido contém o identificador do pedido e os produtos que precisam ser reservados.

#### 2. Validação

A camada de serviço busca cada produto através de seu `UUID` e verifica se a quantidade disponível atende à solicitação:

```text
qt_estoque >= quantidadeComprada
```

Caso o produto não exista ou o saldo seja insuficiente, uma exceção de negócio é lançada.

#### 3. Baixa e Persistência

Após a validação, a quantidade disponível é decrementada e a alteração é persistida através do:

```java
produtoRepository.save()
```

A operação ocorre de maneira transacional para garantir a consistência do estoque.

#### 4. Notificação

Após o processamento com sucesso, o serviço publica o evento:

```text
PedidoAprovadoEvent
```

na exchange:

```text
pedido.exchange
```

utilizando a routing key:

```text
pedido.aprovar.rk
```

---

# 🔄 Saga Pattern — Coreografia

O **Inventory Service** participa da Saga sem possuir um orquestrador central.

Cada microsserviço reage aos eventos de forma independente.

```text
                    EVENT-DRIVEN SAGA
                           │
                           ▼
                  ┌─────────────────┐
                  │  Order Service  │
                  └────────┬────────┘
                           │
                           │ BaixaEstoqueEvent
                           ▼
                     ┌───────────┐
                     │ RabbitMQ  │
                     └─────┬─────┘
                           │
                           ▼
                  ┌─────────────────┐
                  │ Estoque Service │
                  └────────┬────────┘
                           │
                  ┌────────┴────────┐
                  │                 │
                  ▼                 ▼
             Estoque OK       Estoque inválido
                  │                 │
                  ▼                 ▼
       PedidoAprovadoEvent     Exceção de negócio
                  │
                  ▼
             RabbitMQ
                  │
                  ▼
           Order Service
```

O serviço de estoque mantém seu próprio contexto e não precisa conhecer detalhes internos do **Order Service**.

---

# 📡 Mapeamento de Mensageria

| Componente | Identificador | Função |
|---|---|---|
| **Exchange** | `pedido.exchange` | Direct Exchange compartilhada |
| **Fila de Consumo** | `estoque.baixar.queue` | Recebe solicitações de débito de estoque |
| **Routing Key de Saída** | `pedido.aprovar.rk` | Notifica o sucesso da operação |
| **Dead Letter Exchange** | `estoque.dlx` | Tratamento e descarte controlado de mensagens inválidas |

### Fluxo de Mensageria

```text
BaixaEstoqueEvent
       │
       ▼
pedido.exchange
       │
       │ estoque.baixar.queue
       ▼
Estoque Service
       │
       │ Processamento
       ▼
PedidoAprovadoEvent
       │
       │ pedido.aprovar.rk
       ▼
pedido.exchange
       │
       ▼
Order Service
```

---

# ☠️ Dead Letter Exchange

O serviço também utiliza uma **Dead Letter Exchange (DLX)** para lidar com mensagens que não puderam ser processadas corretamente.

```text
estoque.dlx
```

O mecanismo permite o tratamento e descarte controlado de mensagens inválidas, evitando que mensagens problemáticas permaneçam sendo processadas indefinidamente.

---

# 🗄️ Modelo de Dados — PostgreSQL

O serviço possui um banco de dados independente, contendo somente informações relacionadas ao domínio de estoque.

### Tabela `produto`

```sql
CREATE TABLE produto (
    id UUID PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    valor NUMERIC(10, 2) NOT NULL,
    qt_estoque INTEGER NOT NULL CHECK (qt_estoque >= 0),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);
```

### Estrutura

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Identificador único do produto |
| `nome` | `VARCHAR(150)` | Nome do produto |
| `valor` | `NUMERIC(10,2)` | Valor do produto |
| `qt_estoque` | `INTEGER` | Quantidade disponível em estoque |
| `ativo` | `BOOLEAN` | Indica se o produto está ativo |

A restrição:

```sql
CHECK (qt_estoque >= 0)
```

garante que o estoque não possa assumir valores negativos diretamente no banco de dados.

---

# 🚀 Tecnologias

## Backend & Persistência

- ☕ Java 17+
- 🍃 Spring Boot
- 🌱 Spring Data JPA
- 🗃️ Hibernate
- 📦 Maven
- 🐘 PostgreSQL

## Mensageria & Ambiente

- 🐇 RabbitMQ
- `@RabbitListener`
- `RabbitTemplate`
- Event-Driven Architecture
- Saga Pattern
- Comunicação assíncrona
- Dead Letter Exchange (DLX)

## Práticas e Arquitetura

- Clean Code
- Arquitetura em camadas
- Separação de responsabilidades
- Desacoplamento entre microsserviços
- Isolamento de domínio
- Persistência transacional
- Tratamento de exceções
- Validação de regras de negócio

---

# 📦 Contratos de Eventos

## 1. Evento Recebido — `BaixaEstoqueEvent`

O serviço recebe eventos de solicitação de baixa de estoque através do RabbitMQ.

```json
{
  "pedidoId": "55555555-5555-5555-5555-555555555555",
  "itens": [
    {
      "produtoId": "22222222-2222-2222-2222-222222222222",
      "quantidade": 2
    }
  ]
}
```

### Campos

| Campo | Descrição |
|---|---|
| `pedidoId` | Identificador do pedido |
| `itens` | Lista de produtos solicitados |
| `produtoId` | Identificador do produto |
| `quantidade` | Quantidade solicitada |

---

## 2. Evento Disparado — `PedidoAprovadoEvent`

Quando a baixa de estoque é realizada com sucesso, o serviço publica um evento de aprovação.

```json
{
  "pedidoId": "55555555-5555-5555-5555-555555555555"
}
```

Esse evento é enviado através da:

```text
Exchange: pedido.exchange
Routing Key: pedido.aprovar.rk
```

---

# ⚠️ Regras de Negócio

O processamento do estoque segue algumas regras fundamentais:

### Produto inexistente

Caso o `produtoId` informado não exista no banco de dados, o processamento é interrompido e uma exceção de negócio é lançada.

### Estoque insuficiente

A baixa somente pode ser realizada quando:

```text
qt_estoque >= quantidadeComprada
```

Caso contrário, a operação é rejeitada.

### Estoque nunca negativo

O banco de dados possui uma restrição adicional:

```sql
CHECK (qt_estoque >= 0)
```

garantindo que a quantidade de estoque não possa ser inferior a zero.

### Produto inativo

Produtos que não estão disponíveis para utilização podem ser tratados de acordo com as regras de negócio da aplicação, evitando operações indevidas sobre itens desativados.

---

# 🛠️ Como Executar

## Pré-requisitos

Antes de executar o projeto, certifique-se de possuir:

- **Java 17+** ou **Java 21+** instalado
- **Maven** configurado
- **RabbitMQ** em execução
- **PostgreSQL** em execução
- Carga inicial de produtos na tabela `produto` para realização dos testes

---

## 📥 Clone o Repositório

```bash
git clone https://github.com/seu-usuario/ms-estoque.git

cd ms-estoque
```

---

## ▶️ Execute a Aplicação

Utilizando o Maven Wrapper:

### Linux / macOS

```bash
./mvnw clean spring-boot:run
```

### Windows

```bash
mvnw.cmd clean spring-boot:run
```

---

# 🧪 Preparando o Banco para Testes

Antes de iniciar os testes de consumo de eventos, certifique-se de que existam produtos cadastrados.

Exemplo:

```sql
INSERT INTO produto (
    id,
    nome,
    valor,
    qt_estoque,
    ativo
) VALUES (
    '22222222-2222-2222-2222-222222222222',
    'Produto de Teste',
    750.00,
    10,
    TRUE
);
```

A partir desse produto, é possível enviar um `BaixaEstoqueEvent` solicitando uma quantidade disponível.

---

# 📌 Responsabilidades do Serviço

O **Inventory Service** é responsável por:

```text
                  INVENTORY SERVICE
                         │
          ┌──────────────┼──────────────┐
          │              │              │
          ▼              ▼              ▼
       RabbitMQ      PostgreSQL      Regras de
          │              │            negócio
          │              │              │
          ▼              ▼              ▼
      Consumo       Persistência    Validação
      de eventos      do estoque    de estoque
          │              │              │
          └──────────────┼──────────────┘
                         │
                         ▼
                 Evento de aprovação
                         │
                         ▼
                    RabbitMQ
```

O serviço mantém seu próprio banco de dados e não possui dependência direta com a estrutura interna do banco de pedidos.

Essa separação permite que cada microsserviço mantenha seu próprio domínio e evolua de maneira independente.

---

# 🎯 Objetivo do Projeto

Este projeto tem como objetivo demonstrar, na prática, a construção de um microsserviço de estoque utilizando conceitos de arquiteturas distribuídas modernas, como:

- Microsserviços
- Event-Driven Architecture
- Saga Pattern
- Coreografia
- RabbitMQ
- Comunicação assíncrona
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Transações
- Regras de negócio
- Isolamento de domínio
- Resiliência em sistemas distribuídos

---

# 📚 Conceitos Demonstrados

```text
Spring Boot
     │
     ├── Spring Data JPA
     │
     ├── PostgreSQL
     │
     └── RabbitMQ
            │
            ├── @RabbitListener
            ├── RabbitTemplate
            ├── Exchanges
            ├── Queues
            ├── Routing Keys
            └── Dead Letter Exchange
                    │
                    ▼
              Saga Pattern
                    │
                    ▼
          Event-Driven Architecture
                    │
                    ▼
             Coreografia
```

---

# 🔐 Isolamento de Domínio

O **Inventory Service** possui autonomia sobre os dados relacionados ao estoque.

```text
┌─────────────────────┐
│    Order Service    │
│                     │
│    PostgreSQL       │
└──────────┬──────────┘
           │
           │ Eventos
           ▼
      ┌───────────┐
      │ RabbitMQ  │
      └─────┬─────┘
            │
            │ Eventos
            ▼
┌─────────────────────┐
│  Inventory Service  │
│                     │
│    PostgreSQL       │
└─────────────────────┘
```

O serviço de estoque:

- Não acessa o banco de dados de pedidos
- Não conhece as entidades internas do Order Service
- Não depende diretamente de sua implementação
- Comunica-se através de contratos de eventos
- Mantém a responsabilidade exclusiva sobre o estoque

Esse isolamento reduz o acoplamento e favorece a independência dos microsserviços.

---

# 👨‍💻 Autor

**Arnaldo Felipe da Silva**

> Projeto desenvolvido com foco em aprendizado e aplicação prática de arquitetura de microsserviços, mensageria, Event-Driven Architecture e sistemas distribuídos.
