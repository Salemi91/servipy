# ServiPy Backend

API REST para la plataforma ServiPy, construida con Java 21 y Spring Boot.

## Requisitos

- Java 21 (JDK)
- MySQL 8 (para ejecución local; tests usan H2 en memoria)
- Docker (opcional, para levantar MySQL con `docker-compose`)

## Variables de entorno

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `DB_URL` | URL JDBC de conexión a MySQL | `jdbc:mysql://localhost:3306/servipy` |
| `DB_USERNAME` | Usuario de base de datos | `servipy_user` |
| `DB_PASSWORD` | Contraseña de base de datos | `servipy_pass` |

Para desarrollo local, puedes crear un archivo `src/main/resources/application-local.yml` (ignorado por git) con los valores directos.

## Cómo ejecutar

```bash
# Levantar MySQL (desde la raíz del proyecto)
docker compose up -d

# Ejecutar el backend con perfil local
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

El servidor arranca en `http://localhost:8080`.

## Cómo ejecutar tests

```bash
./mvnw test
```

Los tests usan el perfil `test` automáticamente, con H2 en memoria. No necesitan MySQL.

## Endpoint de salud

```
GET /api/v1/health
```

Respuesta (HTTP 200):

```json
{
  "status": "UP",
  "application": "servipy-backend"
}
```

Accesible sin autenticación. Útil para verificar que el backend está operativo.

## Arquitectura: Vertical Slicing + Clean Architecture

El backend aplica **Vertical Slicing** por dominio/feature, donde cada feature tiene su propia estructura interna basada en **Clean Architecture**:

```
src/main/java/py/com/servipy/
├── shared/                  # Código transversal (config, excepciones, utilidades)
│   ├── config/              # Configuración de Spring (seguridad, CORS, etc.)
│   ├── exception/           # Manejo global de errores, ErrorResponse
│   └── web/                 # Filtros, interceptors compartidos
├── health/                  # Feature: health check
│   └── infrastructure/
│       └── web/             # Adaptador REST (HealthController)
└── <feature>/               # Cada feature futura sigue esta estructura:
    ├── domain/              # Entidades, value objects, interfaces de puerto
    ├── application/         # Casos de uso (servicios de aplicación)
    └── infrastructure/
        ├── web/             # Adaptadores REST (controllers, DTOs de request/response)
        └── persistence/     # Adaptadores JPA (repositories, entidades JPA)
```

### Principios

- **Dependency Rule**: el dominio no conoce Spring, JPA ni ningún framework.
- **Puertos y adaptadores**: los casos de uso definen interfaces; la infraestructura las implementa.
- **DTOs separados de entidades**: nunca se exponen entidades JPA en la API.
- **Un paquete por feature**: cada funcionalidad es un slice vertical independiente.
- **shared/ para lo transversal**: seguridad, manejo de errores y configuración viven aquí.
