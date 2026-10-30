# Etapa de compilación: incluye Maven y JDK solo durante el build.
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY pom.xml ./
COPY src ./src
# Las descargas de dependencias pueden sufrir cortes transitorios en redes locales.
# Maven reintenta antes de dar por fallida una construcción reproducible.
RUN mvn -B -ntp -Dmaven.wagon.http.retryHandler.count=5 package -DskipTests

# Imagen final mínima: solo JRE y el artefacto ejecutable.
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl \
    && addgroup -S nexo \
    && adduser -S nexo -G nexo

WORKDIR /app
COPY --from=build --chown=nexo:nexo /workspace/target/*.jar app.jar

USER nexo

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=5 \
  CMD curl --fail --silent http://localhost:8080/api/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
