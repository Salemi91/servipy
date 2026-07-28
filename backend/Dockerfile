# --- Stage 1: Build ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Descarga previa de dependencias para aprovechar el caché de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia de código fuente y empaquetado del JAR
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario seguro no-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Optimización de memoria JVM ajustada para contenedores
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]