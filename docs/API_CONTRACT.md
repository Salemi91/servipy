# Contrato inicial de API

Prefijo base: `/api/v1`

## Convenciones

- JSON para solicitudes y respuestas.
- JWT en `Authorization: Bearer <token>`.
- Fechas en ISO 8601.
- Errores con estructura uniforme.
- No devolver entidades JPA directamente; utilizar DTO.
- No cambiar rutas o campos sin acuerdo entre frontend y backend.

## Autenticación

```text
POST /auth/register/client
POST /auth/register/professional
POST /auth/login
GET  /auth/me
```

### Login

Solicitud:

```json
{
  "email": "usuario@ejemplo.com",
  "password": "********"
}
```

Respuesta:

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "name": "Nombre",
    "email": "usuario@ejemplo.com",
    "role": "CLIENT"
  }
}
```

## Categorías

```text
GET  /categories
POST /admin/categories
```

## Profesionales

```text
GET /professionals?categoryId=&cityId=&availability=
GET /professionals/{id}
GET /professional/me
PUT /professional/me
PATCH /professional/me/availability
```

## Servicios ofrecidos

```text
GET    /professionals/{professionalId}/services
GET    /professional/me/services
POST   /professional/me/services
DELETE /professional/me/services/{serviceId}
```

## Solicitudes

```text
POST  /service-requests
GET   /client/service-requests
GET   /professional/service-requests
PATCH /professional/service-requests/{id}/accept
PATCH /professional/service-requests/{id}/complete
PATCH /client/service-requests/{id}/cancel
```

## Administración

```text
GET   /admin/professionals/pending
PATCH /admin/professionals/{id}/approve
PATCH /admin/professionals/{id}/reject
```

## Salud

```text
GET /health
```

Respuesta:

```json
{
  "status": "UP"
}
```

## Formato de error

```json
{
  "timestamp": "2026-07-22T20:00:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "La solicitud contiene datos inválidos",
  "errors": [
    {
      "field": "email",
      "message": "Debe ser un correo válido"
    }
  ]
}
```
