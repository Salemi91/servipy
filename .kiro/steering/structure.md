# Estructura del Monorepo — ServiPy

```
/
├── frontend/          # Angular app
├── backend/           # Spring Boot app (Maven)
├── database/          # schema.sql (charset) + seed.sql (datos demo)
├── docs/              # Documentación vigente
├── .kiro/             # Configuración Kiro (steering, specs)
├── docker-compose.yml
└── PROJECT.md         # Fuente única de verdad
```

## Backend (`/backend`)

Paquete raíz: `py.com.servipy`. Arquitectura: **Vertical Slicing** por dominio, con capas internas por slice.

```
src/main/java/py/com/servipy/
├── shared/                  # Transversal
│   ├── config/              # SecurityConfig, beans de infraestructura
│   ├── exception/           # GlobalExceptionHandler, ErrorResponse, excepciones comunes
│   └── web/                 # Filtros compartidos (JwtAuthenticationFilter)
└── <slice>/
    ├── domain/              # Entidades JPA y enums del dominio
    ├── application/         # Casos de uso
    │   ├── dto/             # Records de request/response
    │   └── exception/       # Excepciones propias del slice
    └── infrastructure/
        ├── web/             # Controllers REST
        ├── persistence/     # Repositorios Spring Data y Specifications
        └── storage/         # Adaptadores de almacenamiento
```

Slices actuales: `auth`, `user`, `category`, `city`, `country`, `client`, `professional`, `servicerequest`, `health`, `shared`.

### Reglas

- Paquetes, clases, métodos y variables **en inglés**.
- Un slice es dueño de sus datos. No se inyecta el repositorio de otro slice: se consume su capa de aplicación.
- Un concepto de dominio se define una sola vez. No se duplican entidades ni enums entre slices.
- DTOs (`record`) separados de entidades; nunca se serializa una entidad JPA.
- La capa de aplicación usa repositorios, no `EntityManager` ni SQL directo.
- Las reglas de negocio viven en `domain/` o `application/`, nunca en el controller ni en el repositorio.
- Inyección por constructor con campos `final`.
- Sin interfaz + `Impl` cuando hay una sola implementación y no existe un puerto real que aislar.
- El concepto de solicitud de servicio se llama `ServiceRequest` en todo el código. No se usa el término `booking`.

## Frontend (`/frontend`)

```
src/app/
├── core/              # Singletons: auth, http (ApiService, interceptors), guards, config, layout
├── shared/            # Componentes reutilizables, models, utilities
└── features/
    ├── public/        # Home y catálogo público
    ├── authentication/# Login, registro
    ├── client/        # Perfil e historial del cliente
    ├── professional/  # Onboarding y gestión de solicitudes
    └── administration/# Panel de administración
```

### Reglas

- Componentes standalone; sin NgModules.
- Lazy loading por feature vía `loadChildren`.
- Imports mediante los alias `@core/*`, `@shared/*`, `@features/*`, `@env`. Prohibidas las rutas relativas profundas (`../../../`).
- Un feature no importa código interno de otro feature: lo compartido sube a `shared/` o `core/`.
- Todo acceso HTTP pasa por `ApiService`; los componentes no inyectan `HttpClient` ni escriben URLs del backend.
- Sin `any`. Sin datos mock en código de producción.
