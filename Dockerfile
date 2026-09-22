# Usa a imagem oficial do Java
FROM eclipse-temurin:21-jdk-alpine

# Cria uma pasta para a aplicação dentro do contêiner
WORKDIR /app

# Copia o arquivo .jar gerado pelo Maven para dentro do contêiner
COPY target/*.jar app.jar

# Comando que será executado quando o contêiner iniciar
ENTRYPOINT ["java", "-jar", "app.jar"]