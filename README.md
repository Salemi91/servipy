# ServiPy

ServiPy conecta clientes con profesionales de servicios (plomería, electricidad, limpieza, pintura, jardinería, carpintería y más) en Paraguay. Un cliente busca por categoría y ciudad, consulta el perfil y el tarifario de un profesional, envía una solicitud y hace seguimiento de su estado. El profesional gestiona su tarifario y resuelve las solicitudes que recibe. El administrador modera qué profesionales quedan visibles en el catálogo.

Desarrollado por **ÑandeCódigo Inc.** para la Hackathon Kiro AI 2026.

## Roles

| Rol | Qué hace |
|-----|----------|
| `CLIENT` | Busca profesionales, envía solicitudes, sigue su historial, gestiona su perfil. |
| `PROFESSIONAL` | Publica su perfil y tarifario, define disponibilidad, acepta o rechaza solicitudes. |
| `ADMIN` | Aprueba o rechaza profesionales, gestiona categorías. Hereda acceso a todo. |

Un profesional recién registrado queda en estado `PENDING` y no aparece en el catálogo hasta que un ADMIN lo aprueba. Una solicitud aceptada habilita el contacto directo por WhatsApp entre cliente y profesional.

## Arquitectura

```
┌─────────────────┐      HTTPS       ┌──────────────────┐      JDBC      ┌───────────┐
│  Angular 19 SPA  │ ───────────────▶ │  Spring Boot 3.3  │ ─────────────▶ │  MySQL 8  │
│  (Nginx estático) │  /api/v1/...    │  (Vertical Slicing)│                │            │
└─────────────────┘                  └──────────────────┘                └───────────┘
```

- **Frontend**: Angular 19 con componentes standalone, Signals, Tailwind CSS. Servido como estático detrás de Nginx en producción; `ng serve` en desarrollo.
- **Backend**: Spring Boot 3.3 sobre Java 21, organizado en slices verticales (`auth`, `user`, `category`, `city`, `professional`, `client`, `servicerequest`, `shared`) bajo el paquete `py.com.servipy`. Autenticación con JWT y autorización por rol.
- **Base de datos**: MySQL 8, esquema versionado con Flyway.
- **Infraestructura**: Docker Compose orquesta los tres servicios (`mysql_db`, `spring_backend`, `angular_frontend`); Nginx actúa como proxy inverso sirviendo el frontend y reenviando `/api/` al backend.

El detalle completo de reglas de negocio, contrato de API y convenciones vive en [`PROJECT.md`](PROJECT.md).

## Guía de inicio rápido

### Requisitos previos

- Docker y Docker Compose
- Node.js 20+ (recomendado 22 LTS) y npm 10+ — solo si vas a correr el frontend fuera de Docker
- Java 21 (JDK) y Maven Wrapper incluido — solo si vas a correr el backend fuera de Docker

### Opción 1: todo con Docker Compose

```bash
cp .env.example .env
# Editar .env si es necesario (valores de desarrollo ya vienen listos)

./deploy.sh
# o, sin el script de espera/seed:
docker compose up -d
```

Esto levanta MySQL, el backend (Flyway migra el esquema automáticamente) y el frontend servido por Nginx en `http://localhost` (puerto 80). El backend queda accesible en `http://localhost:8080/api/v1`.

### Opción 2: solo MySQL en Docker, backend y frontend en local

Útil para desarrollo activo, con recarga en caliente en ambos lados.

```bash
docker run -d --name servipy-mysql-dev -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=servipy_dev_root -e MYSQL_DATABASE=servipy \
  -e MYSQL_USER=servipy_user -e MYSQL_PASSWORD=servipy_dev_pass \
  -v servipy_mysql_dev_data:/var/lib/mysql \
  mysql:8.0 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

cd backend && ./mvnw spring-boot:run    # perfil "local" por defecto, http://localhost:8080

cd frontend && npm install && npm start # http://localhost:4200
```

Ver [`backend/README.md`](backend/README.md) y [`frontend/README.md`](frontend/README.md) para el detalle de cada lado.

### Cargar datos de prueba

```bash
mysql -h 127.0.0.1 -u servipy_user -pservipy_dev_pass servipy < database/seed.sql
```

### Credenciales demo (`database/seed.sql`)

Contraseña para todas las cuentas: `password123`.

| Rol | Email | Notas |
|-----|-------|-------|
| ADMIN | `admin@servipy.com` | Modera profesionales y categorías. |
| CLIENT | `carlos@example.com`, `maria@example.com`, `juan@example.com` | María tiene una solicitud `ACCEPTED` para probar el contacto por WhatsApp. |
| PROFESSIONAL | `roberto@example.com`, `ana@example.com`, `pedro@example.com`, `laura@example.com` | Perfiles `APPROVED`, visibles en el catálogo. |
| PROFESSIONAL (pendiente) | `diego@example.com` | Perfil `PENDING`, a propósito no visible en el catálogo hasta que el admin lo apruebe. |

## Documentación

- **[PROJECT.md](PROJECT.md) — fuente única de verdad**: visión, reglas de negocio, stack, arquitectura, contrato de API y convenciones.
- [Modelo de dominio](docs/DOMAIN_MODEL.md)
- [Contrato de API](docs/API_CONTRACT.md)
- [Estrategia de ramas](docs/BRANCHING.md)
- [Definición de terminado](docs/DEFINITION_OF_DONE.md)
- [Backend: setup y arquitectura](backend/README.md) · [Frontend: setup y estructura](frontend/README.md)
