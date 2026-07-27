# ==========================================
# ESTÁGIO 1: BUILD (Compilação)
# ==========================================
# Usamos a imagem oficial do Java 17 (Alpine é uma versão Linux super leve)
FROM eclipse-temurin:17-jdk-alpine AS builder

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia os arquivos do Maven (pom.xml e mvnw) primeiro.
# Isso aproveita o cache do Docker para não baixar dependências toda hora.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Baixa as dependências offline (usando cache)
RUN ./mvnw dependency:go-offline

# Agora copia o código fonte do projeto
COPY src src

# Compila o projeto gerando o arquivo .jar (ignorando testes para ser mais rápido)
RUN ./mvnw package -DskipTests

# ==========================================
# ESTÁGIO 2: PRODUÇÃO (Execução)
# ==========================================
# Usamos apenas o JRE (Java Runtime Environment) que é muito mais leve que o JDK
FROM eclipse-temurin:17-jre-alpine

# Cria um usuário não-root por segurança (Boas práticas Enterprise)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copia o arquivo .jar gerado no Estágio 1 (builder) para este estágio limpo
COPY --from=builder /app/target/*.jar app.jar

# Expõe a porta 8080 para o mundo exterior
EXPOSE 8080

# Comando que o Docker vai executar ao iniciar o contêiner
ENTRYPOINT ["java", "-jar", "app.jar"]
