# ServiPy

ServiPy es una aplicación web que conecta clientes con profesionales de servicios en Paraguay, permitiendo buscar por categoría y ciudad, consultar perfiles, solicitar servicios y contactar por WhatsApp.

## Estado

Proyecto desarrollado por **ÑandeCódigo Inc.** para la Hackathon Kiro AI 2026.

## MVP

### Cliente
- Registro e inicio de sesión.
- Búsqueda de profesionales por categoría y ciudad.
- Consulta del perfil y tarifario.
- Creación y seguimiento de solicitudes.
- Contacto por WhatsApp después de la aceptación.

### Profesional
- Registro e inicio de sesión.
- Creación y edición de perfil.
- Publicación de servicios y precios.
- Gestión de disponibilidad.
- Recepción, aceptación y finalización de solicitudes.

### Administrador
- Inicio de sesión.
- Aprobación o rechazo de profesionales.
- Creación y listado de categorías.

## Tecnologías

- Frontend: Angular + Tailwind CSS
- Backend: Java + Spring Boot
- Seguridad: Spring Security + JWT
- Base de datos: MySQL
- Infraestructura: AWS sobre Ubuntu Server
- Proxy inverso: Nginx
- Control de versiones: GitHub
- Desarrollo asistido: Kiro

## Estructura prevista

```text
servipy/
├── frontend/
├── backend/
├── database/
├── docs/
├── .kiro/
│   ├── steering/
│   └── specs/
└── .github/
```

## Documentación

- [Alcance del MVP](docs/MVP.md)
- [Contrato inicial de API](docs/API_CONTRACT.md)
- [Modelo de dominio](docs/DOMAIN_MODEL.md)
- [Estrategia de ramas](docs/BRANCHING.md)
- [Definición de terminado](docs/DEFINITION_OF_DONE.md)
- [Primer prompt para Kiro](docs/KIRO_FIRST_PROMPT.md)
