# Build en dos etapas: la imagen final no lleva el JDK completo ni el codigo fuente,
# solo el jar y un JRE minimo - reduce superficie de ataque de una imagen que va a
# tener credenciales/config de conexion a un HSM en runtime.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S hsmclient && adduser -S hsmclient -G hsmclient
WORKDIR /app
COPY --from=build /workspace/target/hsm-thales-client-*.jar app.jar
USER hsmclient
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
