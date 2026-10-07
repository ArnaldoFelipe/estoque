# ESTÁGIO 1: Construção (Build)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copia o pom.xml e baixa as dependências (usa cache do Docker para ir mais rápido)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código fonte e empacota o projeto
COPY src ./src
RUN mvn clean package -DskipTests

# ESTÁGIO 2: Execução (Run)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia o .jar gerado no estágio 1 para a imagem final
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta do Estoque
EXPOSE 8081

# Comando para ligar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]