package com.br.estoque.listener;

import com.br.estoque.dto.rabbit.BaixaEstoqueEvent;
import com.br.estoque.dto.rabbit.ItemBaixaDTO;
import com.br.estoque.services.ProdutoService;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EstoqueListener {

    private final ProdutoService produtoService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "estoque.baixar.queue", durable = "true",
                           arguments = {
                                   @Argument(name = "x-dead-letter-exchange", value = "estoque.dlx"),
                                   @Argument(name = "x-dead-letter-routing-key", value = "estoque.baixar.dlq.rk")
                           }),
            exchange = @Exchange(value = "pedido.exchange", type = "direct", ignoreDeclarationExceptions = "true"),
            key = "estoque.baixar.rk"
    ))
    public void receberMensagem(BaixaEstoqueEvent evento){

        System.out.println("Mensagem Recebida do RabbitMQ! Pedido ID: " + evento.pedidoId());

        for (ItemBaixaDTO itens : evento.itens()){
            System.out.println("Baixando " +itens.quantidade()+ "unidades do produto " + itens.produtoId());
            produtoService.baixarEstoque(itens.produtoId(), itens.quantidade());
        }

        System.out.println("Estoque atualizado com sucesso!");
    }
}
