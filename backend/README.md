# ServiPy Backend

API REST para la plataforma ServiPy.

## Stack y requisitos

- **Java 21** (JDK)
- **Spring Boot 3.3.x** (Web, Validation, Data JPA, Security, Actuator)
- **Maven** con wrapper incluido (`./mvnw`, no requiere Maven instalado)
- **MySQL 8** para ejecución local y producción; **H2 en memoria** para tests
- **Flyway** para migraciones versionadas del esquema
- Docker (opcional, para levantar MySQL sin instalarlo)

## Arquitectura: Vertical Slicing + Clean Architecture

El backend aplica **Vertical Slicing** por dominio bajo el paquete raíz `py.com.servipy`. Cada slice tiene su propia estructura de capas:

```
src/main/java/py/com/servipy/
├── shared/                  # Transversal
│   ├── config/              # SecurityConfig, beans de infraestructura
│   ├── exception/           # GlobalExceptionHandler, ErrorResponse, excepciones comunes
│   └── web/                 # JwtAuthenticationFilter y handlers de error de la cadena de filtros
└── <slice>/
    ├── domain/               # Entidades JPA y enums del dominio
    ├── application/          # Casos de uso
    │   ├── dto/              # Records de request/response
    │   └── exception/        # Excepciones propias del slice
    └── infrastructure/
        ├── web/              # Controllers REST
        ├── persistence/      # Repositorios Spring Data y Specifications
        └── storage/          # Adaptadores de almacenamiento (ej. fotos de perfil)
```

Slices actuales: `auth`, `user`, `category`, `city`, `country`, `client`, `professional`, `servicerequest`, `health`, `shared`.

Reglas de diseño:

- Las reglas de negocio viven en `domain/` o `application/`, nunca en el controller ni en el repositorio.
- DTOs (`record`) separados de entidades; nunca se serializa una entidad JPA.
- Un slice no inyecta el repositorio de otro: consume su capa de aplicación (por ejemplo, `client` lee solicitudes a través de `ServiceRequestService`, del slice `servicerequest`).
- Inyección por constructor con campos `final`.

Ver [`PROJECT.md` § 2.2](../PROJECT.md#22-backend) para el detalle completo de convenciones.

## Configuración y entornos

Los perfiles Spring separan desarrollo local, tests y producción:

| Perfil | Uso | Base de datos | Logs |
|--------|-----|----------------|------|
| `local` (por defecto) | Desarrollo en tu máquina | MySQL local, Flyway activo | DEBUG/TRACE, SQL visible |
| `test` | `./mvnw test` | H2 en memoria, Flyway desactivado | DEBUG |
| `prod` | Despliegue | MySQL por variables de entorno, Flyway activo | WARN/INFO, sin stacktraces |

El perfil se selecciona con `SPRING_PROFILES_ACTIVE` (variable de entorno) o `-Dspring-boot.run.profiles=<perfil>` al ejecutar Maven.

Configuración sensible (`JWT_SECRET`, credenciales de base de datos, `CORS_ALLOWED_ORIGIN`) se inyecta por variables de entorno, definidas en `.env` en la raíz del repo (ver `.env.example`). En `prod` no hay valores por defecto: si falta una variable requerida, la aplicación no arranca. Para desarrollo local también podés crear `src/main/resources/application-local.yml` (ignorado por git) con overrides propios.

El esquema de base de datos es siempre responsabilidad de **Flyway** (`src/main/resources/db/migration`); Hibernate solo valida el mapeo (`ddl-auto: validate`), nunca lo genera.

## Comandos de desarrollo

```bash
# Levantar MySQL (ver README raíz para el comando de Docker de un solo contenedor,
# o docker compose up -d mysql_db desde la raíz del repo)

# Ejecutar el backend (perfil "local" por defecto)
./mvnw spring-boot:run

# Ejecutar con un perfil explícito
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Ejecutar tests (usa el perfil "test" con H2, no requiere MySQL)
./mvnw test

# Compilar sin ejecutar tests
./mvnw package -DskipTests
```

El servidor arranca en `http://localhost:8080`.

## Contrato de API (resumen)

Prefijo `/api/v1`. JSON en request y response. JWT en `Authorization: Bearer <token>`.

| Área | Endpoints principales |
|------|------------------------|
| Salud | `GET /health` (público) |
| Auth | `POST /auth/register/client`, `POST /auth/register/professional`, `POST /auth/login` (públicos), `GET /auth/me` |
| Catálogo | `GET /categories`, `GET /cities`, `GET /professionals` (filtros `categoryId`, `cityId`, `search`), `GET /professionals/{id}` — todo público, sin datos de contacto |
| Perfil profesional | `GET|POST /professional/profile`, `GET|POST /professional/profile/services`, `DELETE /professional/profile/services/{id}` |
| Solicitudes | `POST /professionals/{id}/service-requests` (público); `GET`/`PATCH` restringidos al profesional destinatario |
| Cliente | `GET /client/requests`, `GET /client/requests/{id}`, `GET /client/requests/{id}/contact` (datos de contacto, solo si la solicitud está `ACCEPTED`) |
| Admin | `GET /admin/professionals/pending`, `PATCH /admin/professionals/{id}/approve|reject`, `POST /admin/categories` |

Toda respuesta de error usa la misma estructura, generada por `GlobalExceptionHandler` (para excepciones de negocio) o por los handlers de la cadena de filtros de seguridad (para 401/403 antes de llegar al controller):

```json
{
  "timestamp": "2026-07-22T20:00:00Z",
  "status": 404,
  "code": "NOT_FOUND",
  "message": "Solicitud no encontrada",
  "errors": []
}
```

El contrato completo, con todos los códigos de error y las reglas de autorización, está en [`PROJECT.md` § 2.4 y § 2.5](../PROJECT.md#24-contrato-de-api).

## Endpoint de salud

```
GET /api/v1/health
```

```json
{ "status": "UP", "application": "servipy-backend" }
```

Accesible sin autenticación.
