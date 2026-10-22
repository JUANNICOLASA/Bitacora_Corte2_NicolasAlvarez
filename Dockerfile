# S10 - Dockerfile: instrucciones para construir la imagen (el "kit de cocina" que se reproduce igual en cualquier sede).
# Imagen -> contenedor corriendo. El registro (Docker Hub) guarda las versiones de la imagen.
# Multietapa: la etapa 1 compila con Maven y la etapa 2 solo lleva el JRE + el .jar (imagen mas liviana).
# docker build -t blue-velvet-api .   |   docker run -p 8080:8080 --env-file .env blue-velvet-api
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
# Primero el pom y las dependencias para aprovechar la cache de capas
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# Etapa 2: Alpine pesa muy poco -> arranque rapido y menos superficie de ataque
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Seguridad: el contenedor no corre como root
RUN addgroup -S bluevelvet && adduser -S bluevelvet -G bluevelvet
COPY --from=builder /app/target/*.jar app.jar
USER bluevelvet
# Los secretos NO van dentro de la imagen, llegan como variables de entorno al correr el contenedor
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=America/Bogota", "-jar", "app.jar"]
