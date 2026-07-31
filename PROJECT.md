# PROJECT.md — ServiPy

> Fuente única de verdad del proyecto. Consolida `README.md`, `backend/README.md`, `frontend/README.md`, `docs/**`, `.kiro/steering/**`, `.kiro/specs/**` y `.github/**`.
> Ante cualquier discrepancia con otro documento del repositorio, este archivo prevalece.
> Última consolidación: 2026-07-29.

---

## 1. Visión General y Reglas del Negocio

### 1.1 Propósito

ServiPy es una aplicación web responsive que conecta **clientes** con **profesionales de servicios** (plomería, electricidad, limpieza, pintura, jardinería, carpintería, etc.) en Paraguay. Permite buscar profesionales por categoría y ciudad, consultar perfiles y tarifarios, enviar solicitudes de servicio y hacer seguimiento de su estado.

Desarrollado por **ÑandeCódigo Inc.** para la Hackathon Kiro AI 2026. El criterio rector es: funcionalidad demostrable sobre perfección visual, y la solución más simple posible.

### 1.2 Roles

| Rol | Autoridad |
|-----|-----------|
| `CLIENT` | Busca profesionales, envía solicitudes, consulta su historial, gestiona su perfil. |
| `PROFESSIONAL` | Crea su perfil y servicios, define disponibilidad, recibe y resuelve solicitudes. |
| `ADMIN` | Aprueba/rechaza profesionales, gestiona categorías. Hereda acceso a todo. |

Un usuario tiene **exactamente un rol**, asignado en el registro (`/auth/register/client` o `/auth/register/professional`). El ADMIN no se autorregistra: se carga por seed.

### 1.3 Flujos funcionales principales

**Registro y acceso**
1. El usuario se registra como cliente o profesional (nombre, email, contraseña) y recibe un access token JWT.
2. El login devuelve token + datos del usuario; el frontend redirige según rol (`/client`, `/professional`, `/admin`).

**Onboarding del profesional**
1. Tras registrarse, el profesional completa su perfil (teléfono, WhatsApp, descripción, ciudad, disponibilidad) y publica sus servicios con precio y moneda.
2. El perfil nace en estado `PENDING` y **no es visible en el catálogo** hasta que un ADMIN lo apruebe.

**Moderación (ADMIN)**
1. El ADMIN lista profesionales `PENDING`, con nombre, email, teléfono, descripción y fecha de registro.
2. Aprueba (`APPROVED`) o rechaza (`REJECTED`). Solo se transiciona desde `PENDING`.
3. El ADMIN también crea y lista categorías de servicio.

**Búsqueda y solicitud**
1. El cliente busca en el catálogo público filtrando por categoría, ciudad y texto libre.
2. Consulta el perfil del profesional y su tarifario.
3. Envía una solicitud con asunto, descripción, fecha deseada y datos de contacto.
4. Recibe una confirmación con el identificador de la solicitud.

**Gestión de la solicitud (PROFESSIONAL)**
1. El profesional lista las solicitudes recibidas, filtrables por estado.
2. Abre el detalle y **acepta** o **rechaza**.
3. Al aceptar, se habilita el contacto directo por WhatsApp entre las partes.

### 1.4 Reglas y restricciones del dominio

**Identidad y seguridad**
- R1. Un usuario tiene exactamente un rol.
- R2. Nunca se almacenan contraseñas sin hash (BCrypt).
- R3. El hash de contraseña no aparece en ninguna respuesta de la API.
- R4. Una cuenta inactiva (`active = false`) no puede autenticarse ni operar.

**Visibilidad del profesional**
- R5. Un profesional es visible en búsquedas **solo** si `approvalStatus = APPROVED` **y** su usuario está activo.
- R6. Un profesional rechazado no puede tener servicios visibles.
- R7. Solo se listan profesionales que tengan al menos un servicio activo.
- R8. `approvalStatus` solo transiciona `PENDING → APPROVED` o `PENDING → REJECTED`, y únicamente por acción de un ADMIN. Aprobar un perfil ya aprobado es idempotente.
- R9. Un usuario profesional tiene como máximo un perfil profesional.

**Solicitudes de servicio**
- R10. Una solicitud solo puede crearse contra un profesional activo y aprobado; en caso contrario, 404.
- R11. Toda solicitud nace en estado `PENDING` con timestamp de creación.
- R12. `PENDING → ACCEPTED | REJECTED`. Los estados `ACCEPTED` y `REJECTED` son terminales: cualquier intento posterior de transición devuelve 409.
- R13. Todo cambio de estado registra `updatedAt`.
- R14. Solo el profesional destinatario consulta, acepta o rechaza sus solicitudes. Ninguna consulta debe devolver solicitudes de otro profesional.
- R15. Solo el cliente propietario consulta (o cancela) su solicitud.
- R16. El contacto por WhatsApp se habilita cuando la solicitud está `ACCEPTED`. El teléfono y el WhatsApp del profesional no se exponen en el catálogo público: se entregan en `GET /client/requests/{id}/contact`, que verifica pertenencia y estado y devuelve 409 `CONTACT_NOT_AVAILABLE` si la solicitud aún no fue aceptada.
- R17. El detalle de una solicitud no expone identificadores internos de entidades relacionadas ni metadatos de servidor.

**Datos y validación**
- R18. `description` de una solicitud: obligatoria, máximo 2000 caracteres. `name`, `email` y `subject` obligatorios.
- R19. Toda entrada se valida en backend con Bean Validation antes de persistir; los errores se devuelven campo por campo.
- R20. Las categorías se crean siempre con `active = true`.
- R21. Los precios se expresan con moneda explícita (por defecto `PYG`).
- R22. Nunca se exponen entidades JPA en la API: siempre DTOs.

**Estados canónicos**
- `Role`: `CLIENT | PROFESSIONAL | ADMIN`
- `ApprovalStatus`: `PENDING | APPROVED | REJECTED`
- `Availability`: `PRESENCIAL | VIRTUAL | AMBOS`
- `RequestStatus`: `PENDING | ACCEPTED | REJECTED` en el MVP. `COMPLETED` y `CANCELLED` están descritos en `docs/DOMAIN_MODEL.md`, el MVP del README y la spec `customer-profile`, pero **no forman parte del alcance implementado**. Ver §4.

### 1.5 Modelo de dominio

```
Country 1─* City 1─* ProfessionalProfile *─1 User
                          │
                          ├─* OfferedService *─1 Category
                          └─* ServiceRequest
```

| Entidad | Campos relevantes |
|---------|-------------------|
| `User` | id, name, email (único), passwordHash, role, active, phone, photoUrl, createdAt, updatedAt |
| `Country` | id, name, code, defaultCurrency |
| `City` | id, countryId, name, latitude, longitude |
| `Category` | id, name, icon, description, active |
| `ProfessionalProfile` | id, userId (único), photoUrl, phone, whatsapp, description, cityId, approvalStatus, availability, timestamps |
| `OfferedService` | id, professionalId, categoryId, name, description, price, currency, active |
| `ServiceRequest` | id, professionalId, datos de contacto del cliente, subject, description, desiredDate, status, timestamps |

### 1.6 Alcance del MVP

Incluido: registro/login con JWT y roles; catálogo público con filtros; perfil y tarifario del profesional; onboarding del profesional; solicitudes de servicio con máquina de estados; historial y perfil del cliente (datos, foto, contraseña); panel de administración (moderación de profesionales y categorías); endpoint de salud; despliegue con Docker Compose + Nginx.

Fuera de alcance: pagos, mapas, mensajería interna, notificaciones por email, valoraciones, refresh token rotativo, tests E2E.

---

## 2. Stack Tecnológico y Estándares de Arquitectura

### 2.1 Monorepo

```
servipy/
├── frontend/    # Angular SPA
├── backend/     # Spring Boot API
├── database/    # schema.sql (init charset) + seed.sql (datos demo)
├── docs/        # Documentación de proyecto
├── .kiro/       # steering/ (reglas persistentes) + specs/ (features aprobadas)
├── .github/     # Plantilla de PR
├── docker-compose.yml
└── deploy.sh
```

### 2.2 Backend

| Elemento | Valor |
|----------|-------|
| Lenguaje | Java 21 (objetivo declarado; ver §4) |
| Framework | Spring Boot 3.3.x (Web, Validation, Data JPA, Security, Actuator) |
| Build | Maven con wrapper (`./mvnw`) |
| Base de datos | MySQL 8 (`utf8mb4`), H2 en memoria para tests |
| Migraciones | Flyway (`classpath:db/migration`), `ddl-auto: validate` |
| Seguridad | Spring Security + JWT (jjwt 0.12.x), BCrypt |
| Tests | JUnit 5 + Mockito + MockMvc, jqwik para property-based testing |
| Paquete raíz | `py.com.servipy` |
| Prefijo API | `/api/v1` |

**Arquitectura: Vertical Slicing + Clean Architecture.** Un paquete por feature; dentro de cada uno, capas:

```
py/com/servipy/
├── shared/                  # Código transversal (config, excepciones, utilidades)
│   ├── config/              # Configuración de Spring (seguridad, CORS, etc.)
│   ├── exception/           # Manejo global de errores, ErrorResponse
│   └── web/                 # Filtros, interceptors compartidos
├── health/                  # Feature: health check
│   └── infrastructure/
│       └── web/             # Adaptador REST (HealthController)
└── <feature>/
    ├── domain/             # entidades y enums del dominio
    ├── application/        # casos de uso + dto/ + exception/
    └── infrastructure/
        ├── web/            # controllers REST
        ├── persistence/    # repositorios JPA y Specifications
        └── storage/        # adaptadores de almacenamiento
```

Features actuales: `auth`, `user`, `category`, `city`, `country`, `client`, `professional`, `servicerequest`, `health`, `shared`, `health`.

Reglas arquitectónicas exigidas:
- Dependency Rule: la capa de dominio no depende de casos de uso ni de infraestructura.
- Los controladores no contienen lógica de negocio: validan, delegan y traducen a HTTP.
- La capa de aplicación no accede a `EntityManager` ni a APIs de framework de persistencia directamente; usa repositorios.
- DTOs (records) separados de entidades. Nunca se serializa una entidad JPA.
- Cada slice es dueño de sus datos. El acceso entre slices se hace por su capa de aplicación, no por sus repositorios.
- Un único enum/entidad canónica por concepto de dominio; no se duplican entre slices.

### 2.3 Frontend

| Elemento | Valor |
|----------|-------|
| Framework | Angular 19 (standalone components, sin NgModules) |
| Estilos | Tailwind CSS 3 (mobile-first) |
| Estado | Signals de Angular |
| HTTP | `HttpClient` + interceptores funcionales |
| Lenguaje | TypeScript 5.7 en modo `strict` (+ `strictTemplates`, `noUnusedLocals`, `noImplicitReturns`) |
| Tests | Jasmine + Karma (`ChromeHeadless`) |
| Node | 20+ (recomendado 22 LTS), npm 10+ |

```
src/app/
├── core/          # singletons: auth, http (ApiService, interceptors), guards, config, layout
├── shared/        # componentes reutilizables, models, utilities
└── features/      # slices: public, authentication, client, professional, administration
```

Reglas:
- Lazy loading por feature vía `loadChildren`; rutas protegidas con `authGuard` + `roleGuard` (`data: { roles: [...] }`).
- La URL base de la API vive en `environments/` y se inyecta mediante el token `API_BASE_URL`; ningún componente escribe URLs del backend.
- Todo acceso HTTP pasa por un servicio de feature que usa `ApiService`; los componentes no inyectan `HttpClient`.
- Aliases de import: `@core/*`, `@shared/*`, `@features/*`, `@env`.
- Un feature no importa código interno de otro feature; lo compartido sube a `shared/` o `core/`.

### 2.4 Contrato de API

Prefijo `/api/v1`. JSON en request y response. JWT en `Authorization: Bearer <token>`. Fechas ISO 8601.

| Área | Endpoints |
|------|-----------|
| Salud | `GET /health` (público) |
| Auth | `POST /auth/register/client`, `POST /auth/register/professional`, `POST /auth/login` (públicos), `GET /auth/me` |
| Categorías | `GET /categories` (público), `POST /admin/categories` (ADMIN) |
| Ciudades | `GET /cities` (público) |
| Catálogo | `GET /professionals` (público, filtros `categoryId`, `cityId`, `search`, `page`, `size`), `GET /professionals/{id}` (público, sin datos de contacto), `GET /professionals/category-counts` (público, conteo de profesionales visibles por categoría para el home) |
| Perfil profesional | `GET /professional/profile/me`, `POST /professional/profile` (PROFESSIONAL) |
| Tarifario | `GET|POST /professional/profile/services`, `DELETE /professional/profile/services/{id}` (PROFESSIONAL) |
| Solicitudes | `POST /professionals/{professionalId}/service-requests` (público); `GET` de lista y detalle y `PATCH .../{requestId}/status` (PROFESSIONAL destinatario) |
| Cliente | `GET|PUT /client/profile`, `PUT /client/profile/photo`, `PUT /client/profile/password`, `GET /client/requests`, `GET /client/requests/{id}`, `GET /client/requests/{id}/contact` (CLIENT) |
| Admin | `GET /admin/professionals/pending`, `PATCH /admin/professionals/{id}/approve`, `PATCH /admin/professionals/{id}/reject` (ADMIN) |

**Formato de error uniforme** (todas las respuestas de error, sin excepción):

```json
{
  "timestamp": "2026-07-22T20:00:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Error de validación en los datos enviados",
  "errors": [{ "field": "email", "message": "Debe ser un correo válido" }]
}
```

Códigos en uso: `VALIDATION_ERROR`, `MALFORMED_REQUEST`, `INVALID_PARAMETER`, `INVALID_CREDENTIALS`, `UNAUTHORIZED`, `TOKEN_EXPIRED`, `FORBIDDEN`, `ACCOUNT_INACTIVE`, `NOT_FOUND`, `RESOURCE_NOT_FOUND`, `DUPLICATE_EMAIL`, `DUPLICATE_RESOURCE`, `INVALID_STATE_TRANSITION`, `CONTACT_NOT_AVAILABLE`, `METHOD_NOT_ALLOWED`, `INVALID_FILE_TYPE`, `FILE_TOO_LARGE`, `INVALID_CURRENT_PASSWORD`, `INTERNAL_ERROR`.

Los errores originados en la cadena de filtros (sin token, token expirado, rol insuficiente) usan la misma estructura: no hay respuestas de error vacías.

Ningún cambio de ruta o de campo se hace sin acuerdo explícito entre frontend y backend.

### 2.5 Seguridad

- **Access token JWT**: vida corta (30 min por defecto, `JWT_EXPIRATION_MINUTES`). Claims: `sub` (userId), `email`, `role`, `iat`, `exp`. Firma HS256 con secreto de ≥32 bytes.
- **Endpoints públicos**: health, login, registro, `GET /categories`, `GET /cities`, catálogo de profesionales y `POST` de creación de solicitudes. Todo lo demás requiere token válido. La regla de creación pública se declara por método y ruta exacta, antes del comodín del catálogo, para que ningún `GET` de solicitudes quede expuesto.
- **Autorización**: `ROLE_ADMIN` sobre `/api/v1/admin/**` en el `SecurityFilterChain`; `@PreAuthorize` a nivel de controlador para CLIENT y PROFESSIONAL. Cada usuario accede solo a sus propios recursos.
- **Sesión**: stateless (`SessionCreationPolicy.STATELESS`), CSRF deshabilitado por ser API sin cookies de sesión.
- **CORS**: un único origen configurable (`CORS_ALLOWED_ORIGIN`); métodos GET, POST, PUT, PATCH, DELETE, OPTIONS; headers `Authorization` y `Content-Type`.
- **Uploads**: máximo 5 MB; tipos permitidos `image/jpeg`, `image/png`, `image/webp`, verificados por magic bytes y no solo por `Content-Type`; nombre de archivo generado con UUID.
- **Secretos**: prohibidos en el repositorio. Se usan variables de entorno o archivos ignorados por git (`.env`, `application-local.yml`). `.gitignore` cubre `*.env`, `.env.*`, `application-local.yml`, `*.key`, `*.pem`, `*.p12`.
- **Logging**: nunca se registran tokens ni datos sensibles.
- **Producción**: HTTPS obligatorio.

> Refresh token de vida larga en cookie httpOnly: descrito en `.kiro/steering/security.md`, **no implementado**. Ver §4.

### 2.6 Base de datos

- Migraciones versionadas en `backend/src/main/resources/db/migration` (`V1__init` … `V4__add_client_profile_fields`). Toda migración es aditiva e inmutable una vez integrada en `main`.
- El esquema es la fuente de verdad; Hibernate solo valida (`ddl-auto: validate`).
- `database/schema.sql` fija el charset del contenedor MySQL; `database/seed.sql` carga datos demo (países, ciudades, 10 categorías, usuarios de prueba por rol incluido `admin@servipy.com`, perfiles, servicios y solicitudes). El seed **no se ejecuta en producción**.
- Índices obligatorios sobre columnas de filtrado frecuente (`approval_status`, `professional_id`, `status`, `active`, `created_at`).

### 2.7 Infraestructura y despliegue

- Servidor Ubuntu en AWS.
- `docker-compose.yml` levanta tres servicios en la red `servipy-net`: `mysql_db` (volumen `mysql_data`, healthcheck), `spring_backend` (depende del healthcheck de MySQL, volumen `uploads_data`, healthcheck sobre `/actuator/health`) y `angular_frontend` (Nginx en el puerto 80).
- Nginx sirve la SPA (`try_files … /index.html`), aplica gzip y cache de assets, y proxya `/api/` y `/actuator/health` al backend.
- Toda la configuración sensible se inyecta por variables de entorno definidas en `.env` (plantilla: `.env.example`): `MYSQL_*`, `JWT_SECRET`, `JWT_EXPIRATION_MINUTES`, `CORS_ALLOWED_ORIGIN`, `SPRING_PROFILES_ACTIVE`.
- `deploy.sh` carga `.env`, levanta la pila, espera a que Flyway termine y aplica el seed.
- Perfiles Spring: `local` (desarrollo), `test` (H2), `prod` (todo por variables de entorno).

### 2.8 CI/CD

Estado actual: **no existe pipeline automatizado**; `.github/` solo contiene la plantilla de PR. Las verificaciones son manuales:

```bash
# Backend
cd backend && ./mvnw test
# Frontend
cd frontend && ng test --watch=false --browsers=ChromeHeadless && npm run lint
```

Pipeline objetivo (pendiente): build + tests de backend y frontend en cada PR, con la cobertura como métrica informativa y no bloqueante.

---

## 3. Guía de Estilo y Convenciones

### 3.1 Principios generales

- **Clean Code**: nombres expresivos y pronunciables, funciones cortas con un solo nivel de abstracción, sin números mágicos, sin comentarios que expliquen lo que el código ya dice. Un comentario que contradice al código es un defecto.
- **SOLID**: responsabilidad única por clase/componente; extensión sin modificación (p. ej. `Specification` composables); dependencias hacia abstracciones inyectadas por constructor.
- **DRY**: un concepto de dominio se define una sola vez. Duplicar un enum, una entidad o una regla es una violación, no un atajo.
- **YAGNI**: no se implementa nada que no esté en una spec aprobada. Sin capas, interfaces o parámetros "por si acaso".
- **Bajo acoplamiento, alta cohesión**: cada slice es autónomo; la comunicación entre slices es explícita y mínima.
- **Deuda técnica**: se registra en la spec correspondiente con su condición de cierre. No se dejan mocks, placeholders ni endpoints abiertos "temporalmente" sin una tarea que los cierre.

### 3.2 Convenciones backend

- Paquetes en inglés y minúsculas; clases en `PascalCase`; métodos y variables en `camelCase`; constantes en `UPPER_SNAKE_CASE`.
- Sufijos consistentes: `*Controller`, `*Service`, `*Repository`, `*Request`, `*Response`, `*Dto`, `*Exception`, `*Specification`.
- Inyección **por constructor** y campos `final`. Sin `@Autowired` en campos.
- DTOs como `record` con anotaciones de Bean Validation y mensajes en español.
- Servicios anotados con `@Transactional(readOnly = true)` a nivel de clase y `@Transactional` en los métodos de escritura.
- Sin interfaz + `Impl` cuando existe una sola implementación y no hay puerto real que aislar; se prefiere la clase concreta.
- Excepciones de negocio propias, semánticamente precisas, traducidas a HTTP exclusivamente en `GlobalExceptionHandler`. Un `try/catch` local solo se justifica para traducir una excepción técnica a una de dominio.
- Reglas de negocio en la capa de aplicación o en el dominio, nunca en el controlador ni en el repositorio.
- Nomenclatura de tests: `should_<resultado>_when_<condición>`, patrón AAA (Arrange, Act, Assert).

### 3.3 Convenciones frontend

- Componentes, servicios y modelos en inglés. Archivos en `kebab-case` con sufijo: `*.component.ts`, `*.service.ts`, `*.guard.ts`, `*.model.ts`, `*.routes.ts`.
- Componentes standalone; `inject()` en lugar de constructor injection; `signal()` para estado local; `readonly` para dependencias.
- Interfaces explícitas para toda respuesta de API; prohibido `any`.
- Formularios con Reactive Forms y validación en tiempo real; mensaje de error junto al campo.
- Estados de UI explícitos y modelados (`loading | loaded | empty | error`), con opción de reintento en error.
- Botón de submit deshabilitado mientras la petición está en curso, para evitar envíos duplicados.
- Accesibilidad: `label` asociado a cada input, `role="alert"` en mensajes de error, `alt` en imágenes, foco visible.

### 3.4 Manejo de errores

| Situación | HTTP | Código |
|-----------|------|--------|
| Validación de entrada | 400 | `VALIDATION_ERROR` (con `errors[]` por campo) |
| Credenciales inválidas | 401 | `INVALID_CREDENTIALS` |
| Token ausente/inválido | 401 | `UNAUTHORIZED` |
| Token expirado | 401 | `TOKEN_EXPIRED` |
| Rol insuficiente | 403 | `FORBIDDEN` |
| Cuenta inactiva | 403 | `ACCOUNT_INACTIVE` |
| Recurso inexistente o ajeno | 404 | `NOT_FOUND` |
| Email duplicado | 409 | `DUPLICATE_EMAIL` |
| Transición de estado inválida | 409 | `INVALID_STATE_TRANSITION` |
| Fallo no previsto | 500 | `INTERNAL_ERROR` (mensaje genérico, sin stack trace) |

Un recurso que existe pero no pertenece al solicitante se responde como 404, no 403, para no filtrar su existencia.

### 3.5 Estrategia de tests

- Backend: tests unitarios obligatorios en la capa de servicio; tests de integración (MockMvc) en endpoints críticos: autenticación, registro, solicitudes de servicio, moderación.
- Frontend: tests unitarios en componentes con lógica significativa; servicios HTTP con `HttpClientTestingModule`; nomenclatura `it('should <comportamiento esperado>')`.
- Property-based tests (jqwik) para invariantes del dominio: máquina de estados, aislamiento por propietario, validación.
- Sin tests E2E en el MVP. La cobertura se busca en happy paths y no bloquea la integración.

### 3.6 Git y proceso

- Ramas: `main` protegida; `feature/*`, `fix/*`, `docs/*`, `chore/*`. Vida útil ideal < 1 día.
- Commits convencionales: `feat`, `fix`, `docs`, `chore`, `refactor`, `test`, `style`.
- Sin push directo a `main`; sin force-push en ramas compartidas. Todo entra por PR con squash merge, título ≤ 70 caracteres y descripción de qué cambia, por qué y qué se probó.
- Revisión obligatoria por otra persona. La rama se actualiza con `main` antes del merge y se elimina después.
- No se combinan refactors grandes con funcionalidad nueva.

**Definition of Ready**: objetivo claro, criterios de aceptación, rol afectado identificado, dependencias conocidas, contrato de API acordado si cruza frontend y backend, y ≤ 1 día de trabajo.

**Definition of Done**: cumple criterios de aceptación; compila y corre localmente; tiene pruebas proporcionales al riesgo; no rompe pruebas existentes; sin secretos; respeta los steering files; no modifica contratos ajenos sin acuerdo; incluye validaciones y manejo de errores; revisada y aprobada por PR; integrada en `main`; documentación actualizada; funciona en el entorno desplegado cuando afecta la demo.

---

## 4. Inconsistencias Detectadas en la Documentación

### 4.1 Contradicciones

| # | Contradicción | Documentos en conflicto | Resolución adoptada en este documento |
|---|---------------|-------------------------|----------------------------------------|
> Estado tras la refactorización de julio 2026: quedan resueltas D1, D3, D6, D7 (parcial), D8, D9, D10, D11, D13 y D16 (parcial). Se conservan aquí como registro de la decisión adoptada. Siguen abiertas D2, D4, D5 (documental), D12, D14, D15.

| D1 | Estructura de paquetes del backend: `com.servipy` con carpetas `config/`, `security/`, `user/`, `service/`, `booking/`, `common/` frente a `py.com.servipy` con vertical slicing + Clean Architecture. | `.kiro/steering/structure.md` vs `backend/README.md` y código real | Vale vertical slicing sobre `py.com.servipy`. `structure.md` está obsoleto. |
| D2 | "Paquetes en español técnico" frente a paquetes y clases íntegramente en inglés. | `.kiro/steering/tech.md` vs código real | Todo en inglés. |
| D3 | Terminología del dominio: `booking` frente a `service request` / `solicitud`. | `.kiro/steering/testing.md` y `structure.md` vs `docs/DOMAIN_MODEL.md`, specs y código | El concepto único es `ServiceRequest` / solicitud. `booking` no existe. |
| D4 | Autenticación: access + refresh token, refresh en cookie httpOnly, access de 15-30 min. Solo hay access token de 30 min en `localStorage`. | `.kiro/steering/security.md` y `tech.md` vs código real | El MVP usa únicamente access token. El refresh token queda como trabajo futuro. |
| D5 | "Todos los endpoints salvo login/registro/refresh requieren token válido" frente a la nota de la spec `service-request` que declara públicos todos los endpoints de solicitudes por ausencia de JWT. | `.kiro/steering/security.md` vs `.kiro/specs/service-request/design.md` | Manda la regla de seguridad. La excepción de la spec caducó al implementarse JWT: es un hallazgo crítico, no una decisión vigente. Ver auditoría. |
| D6 | Rutas de la API de solicitudes: `POST /service-requests`, `/client/service-requests`, `/professional/service-requests`, `.../accept`, `.../complete`, `.../cancel` frente a `/professionals/{id}/service-requests` con `PATCH .../status`. | `docs/API_CONTRACT.md` vs spec `service-request` y código | Vale el contrato anidado con `PATCH .../status`. `API_CONTRACT.md` describe un contrato que nunca se implementó. |
| D7 | Rutas del perfil profesional: `GET/PUT /professional/me`, `PATCH /professional/me/availability`, `GET/POST/DELETE /professional/me/services` frente a `GET /professional/profile/me` y `POST /professional/profile`, sin endpoints de servicios ni de disponibilidad. | `docs/API_CONTRACT.md` vs código | Vale lo implementado. La gestión de servicios ofrecidos y de disponibilidad no existe todavía. |
| D8 | Rutas del historial del cliente: `GET /client/service-requests` frente a `GET /client/requests`. | `docs/API_CONTRACT.md` vs spec `customer-profile` y código | Vale `/client/requests`. |
| D9 | Filtros del catálogo: `categoryId`, `cityId`, `availability` frente a `categoryId`, `search`. | `docs/API_CONTRACT.md` y README ("búsqueda por categoría y ciudad") vs código | Lo implementado es `categoryId` + `search` + paginación. El filtro por ciudad y disponibilidad es una laguna funcional. |
| D10 | Estados de solicitud: cinco valores (`PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED`) frente a tres. | `docs/DOMAIN_MODEL.md`, README ("finalización de solicitudes"), spec `customer-profile` vs spec `service-request` y código | El MVP implementado tiene tres. `COMPLETED` y `CANCELLED` son alcance no cubierto. |
| D11 | Modelo de `ServiceRequest`: `clientId`, `offeredServiceId`, `address`, `contactPhone`, `preferredDate`, `scheduledDate` frente a `clientName`, `clientEmail`, `clientPhone`, `subject`, `desiredDate` sin vínculo al usuario cliente. | `docs/DOMAIN_MODEL.md` vs spec `service-request`, migración V3 y código | Vale el modelo implementado, con la salvedad de que rompe las reglas R15 y R16. Ver auditoría. |
| D12 | Cliente autenticado como autor de la solicitud frente a "Client: persona no autenticada que envía una solicitud desde el perfil público". | `docs/DOMAIN_MODEL.md` (regla 4) y spec `customer-profile` vs spec `service-request` | Contradicción sin resolver en la documentación original. Este documento adopta el flujo autenticado como objetivo y marca el flujo anónimo como deuda a cerrar. |
| D13 | Java 21 declarado frente a `<java.version>17</java.version>` en el POM. | `.kiro/steering/tech.md`, `backend/README.md`, `docs/TEAM_TASKS_DAY1.md` vs `backend/pom.xml` | El objetivo es Java 21; el POM está desalineado. |
| D14 | "Angular 17+" frente a Angular 19.2 instalado. | `.kiro/steering/tech.md` vs `frontend/package.json` | Angular 19 cumple "17+", pero conviene fijar la versión real. |
| D15 | `docs/DOMAIN_MODEL.md` incluye `Country`/`City` con coordenadas y `Category.icon`, mientras `structure.md` no contempla esos dominios. | steering vs docs | Los dominios `country` y `city` existen y son parte del modelo. |
| D16 | Existencia de CI implícita ("la cobertura no bloquea CI") sin ningún workflow en el repositorio. | `.kiro/steering/testing.md` vs `.github/` | No hay CI. Queda declarado como pendiente. |

### 4.2 Duplicaciones

- `docs/API_CONTRACT.md` (con contenido, parcialmente obsoleto) y `docs/api-contract.md` (plantilla vacía) coexisten. Además de la redundancia, dos archivos que difieren solo en mayúsculas provocan colisiones en sistemas de archivos case-insensitive (Windows, macOS) y en checkouts de Git.
- `.kiro/specs/authentication/` y `.kiro/specs/project-foundation-and-authentication/` cubren el mismo terreno; no está indicado cuál es la vigente.
- El alcance del MVP se enumera en `README.md` y se repite parcialmente en cada spec, sin una fuente única. Este documento pasa a ser esa fuente.
- Las reglas de ramas y commits están duplicadas en `docs/BRANCHING.md` y `.kiro/steering/git-workflow.md`, con listas de tipos de commit distintas (`git-workflow.md` incluye `style`, `BRANCHING.md` no).

### 4.3 Vacíos

- Plantillas sin contenido: `docs/MVP.md`, `docs/architecture.md`, `docs/user-flows.md`, `docs/api-contract.md`. `README.md` enlaza `docs/MVP.md` como alcance del MVP y el enlace no lleva a nada útil.
- `README.md` no enlaza `docs/architecture.md`, `docs/user-flows.md` ni `docs/TEAM_TASKS_DAY1.md`.
- No hay documentación de la regla de negocio de WhatsApp: ni cómo se construye el enlace, ni qué número se usa, ni cómo se oculta antes de la aceptación.
- No hay documentación de la política de expiración/renovación de sesión desde la perspectiva del usuario.
- No se documenta la estrategia de limpieza de archivos subidos (la spec de foto de perfil la delega a "un proceso separado" que no existe ni está planificado).
- `docs/TEAM_TASKS_DAY1.md` conserva fechas y tareas de arranque ya superadas (por ejemplo, "primer despliegue no más tarde del 25 de julio"), sin marca de estado.
- Ningún documento describe cómo se pobla la tabla `offered_services` en producción: no existe endpoint de creación de servicios ofrecidos.

### 4.4 Acciones documentales recomendadas

1. Reescribir `.kiro/steering/structure.md` con la estructura real (vertical slicing, `py.com.servipy`, features actuales) y eliminar las referencias a `booking` y a paquetes en español.
2. Actualizar `.kiro/steering/security.md` y `tech.md`: eliminar el refresh token del alcance vigente o abrir una spec para implementarlo.
3. Eliminar `docs/api-contract.md` y regenerar `docs/API_CONTRACT.md` desde los endpoints reales, o reemplazar ambos por una referencia a §2.4 de este documento.
4. Corregir `docs/DOMAIN_MODEL.md`: estados reales de `ServiceRequest`, campos reales y aclaración sobre la relación con el cliente.
5. Rellenar o eliminar las plantillas vacías de `docs/`, y arreglar los enlaces del `README.md`.
6. Marcar como histórico `docs/TEAM_TASKS_DAY1.md` y consolidar `BRANCHING.md` con `git-workflow.md`.
7. Indicar explícitamente qué spec de autenticación es la vigente y archivar la otra.

---

## 5. Referencias

| Documento | Rol tras la consolidación |
|-----------|---------------------------|
| `PROJECT.md` | Fuente única de verdad. |
| `.kiro/steering/*.md` | Reglas activas para el agente. Requieren las correcciones de §4.4. |
| `.kiro/specs/*/` | Contrato detallado por feature (requirements, design, tasks). Autoridad sobre el detalle de su feature. |
| `docs/DEFINITION_OF_DONE.md` | Vigente. Resumido en §3.6. |
| `docs/BRANCHING.md` | Vigente. Resumido en §3.6. |
| `docs/DOMAIN_MODEL.md` | Parcialmente obsoleto (D10, D11, D12). |
| `docs/API_CONTRACT.md` | Obsoleto (D6, D7, D8, D9). |
| `docs/MVP.md`, `docs/architecture.md`, `docs/user-flows.md`, `docs/api-contract.md` | Vacíos, sin valor. |
| `docs/KIRO_FIRST_PROMPT.md`, `docs/TEAM_TASKS_DAY1.md` | Histórico de arranque. |
| `backend/README.md`, `frontend/README.md` | Vigentes para setup y ejecución local. |
