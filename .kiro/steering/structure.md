# Estructura del Monorepo — ServiPy

```
/
├── frontend/          # Angular app
├── backend/           # Spring Boot app (Maven)
├── docs/              # Documentación del proyecto
└── .kiro/             # Configuración Kiro (steering, hooks, specs)
```

## Backend (`/backend`)
```
src/main/java/com/servipy/
├── config/            # Configuración Spring (CORS, beans)
├── security/          # JWT, filtros, SecurityConfig
├── user/              # Usuarios: entity, repository, service, controller, dto
├── service/           # Servicios profesionales: entity, repo, service, controller, dto
├── booking/           # Solicitudes/reservas: entity, repo, service, controller, dto
└── common/            # Excepciones globales, utilidades compartidas
```
- Un paquete por dominio con sus capas internas.
- DTOs separados de entidades; nunca exponer entidades directamente.

## Frontend (`/frontend`)
```
src/app/
├── core/              # Guards, interceptors, servicios singleton
├── shared/            # Componentes reutilizables, pipes, directivas
├── features/
│   ├── auth/          # Login, registro
│   ├── client/        # Dashboard cliente, búsqueda
│   ├── professional/  # Dashboard profesional, gestión
│   └── admin/         # Panel administración
└── models/            # Interfaces y tipos TypeScript
```
- Lazy loading por feature module.
- Componentes standalone preferidos.
