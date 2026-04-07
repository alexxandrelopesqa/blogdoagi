# Tag da imagem = versão do Playwright no pom.xml
FROM mcr.microsoft.com/playwright/java:v1.58.0-jammy

WORKDIR /app

COPY .mvn ./.mvn
COPY mvnw mvnw.cmd ./
COPY pom.xml .
COPY src ./src

ENV CI=true
ENV HEADLESS=true

RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

RUN chown -R pwuser:pwuser /app
USER pwuser

CMD ["./mvnw", "-B", "clean", "test"]
