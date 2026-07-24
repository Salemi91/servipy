# Stack Tecnológico — ServiPy

## Backend
- Java 21
- Spring Boot 3.x + Spring Security
- Maven (wrapper incluido)
- Autenticación: JWT (access token + refresh token)
- Base de datos: MySQL 8
- API REST bajo el prefijo `/api/v1`

## Frontend
- Angular 17+ (standalone components)
- Tailwind CSS para estilos
- Consumo de API vía HttpClient con interceptors para JWT

## Infraestructura
- Servidor Ubuntu en AWS
- Nginx como proxy inverso (frontend estático + proxy a backend :8080)
- Variables de entorno para configuración sensible

## Convenciones
- Backend: paquetes en español técnico, nombres de clase en inglés (entidades, DTOs, controllers).
- Frontend: componentes y servicios en inglés, lazy loading por feature.
- Ambos: UTF-8, LF como fin de línea.
