# ---------- build ----------
FROM eclipse-temurin:8-jdk AS build

WORKDIR /app

# Dependencias primeiro: esta camada so invalida quando o pom.xml muda.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -B clean package -DskipTests

# ---------- runtime ----------
FROM eclipse-temurin:8-jre-alpine

RUN addgroup -S clareza && adduser -S clareza -G clareza

WORKDIR /app
COPY --from=build /app/target/clareza-api-*.jar app.jar
RUN chown clareza:clareza app.jar

USER clareza
EXPOSE 8080

# Alinha o relogio do sistema ao fuso da aplicacao, para os logs baterem com o que o usuario ve.
# Quem decide as datas de negocio e o bean Clock de ConfiguracaoDataHora, nao esta variavel.
ENV TZ=America/Sao_Paulo

# MaxRAMPercentage: sem isso a JVM calcula o heap errado nos 512 MB do free tier do Render.
# UseSerialGC: o G1 gasta heap em estruturas do coletor sem ganho real nessa escala.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC -Duser.timezone=America/Sao_Paulo"

# O exec faz a JVM virar PID 1 e receber o SIGTERM, permitindo shutdown limpo.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
