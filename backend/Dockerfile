# ---- Stage 1: Build ----
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copiar Maven wrapper y pom.xml primero (cache de dependencias)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copiar código fuente y compilar
COPY src/ src/
RUN ./mvnw package -DskipTests -B

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crear usuario no-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /app/target/*.jar app.jar

# Directorio para uploads
RUN mkdir -p /app/uploads && chown appuser:appgroup /app/uploads

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
