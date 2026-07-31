# Modelo de dominio

Refleja el esquema real (`backend/src/main/resources/db/migration`) y las entidades JPA de `py.com.servipy`. La visión de negocio completa está en [`PROJECT.md` § 1](../PROJECT.md#1-visión-general-y-reglas-del-negocio).

## Relaciones

```
Country 1─* City 1─* ProfessionalProfile 1─1 User
                          │
                          ├─* OfferedService *─1 Category
                          └─* ServiceRequest
```

## User

Tabla `users`. Entidad canónica de usuario; no existe ninguna otra entidad que mapee esta tabla.

- id
- name
- email (único, normalizado a minúsculas)
- passwordHash (BCrypt)
- role: `CLIENT | PROFESSIONAL | ADMIN`
- active
- phone (nullable)
- photoUrl (nullable)
- createdAt
- updatedAt

## Country

Tabla `countries`.

- id
- name
- code (único, ISO alfa-3)
- defaultCurrency

## City

Tabla `cities`.

- id
- countryId
- name
- latitude (nullable)
- longitude (nullable)

## Category

Tabla `categories`.

- id
- name
- icon (nullable)
- description (nullable)
- active

## ProfessionalProfile

Tabla `professional_profiles`. Relación 1–1 con `User` (`user_id` único).

- id
- userId
- photoUrl (nullable)
- phone
- whatsapp
- description
- cityId (nullable)
- approvalStatus: `PENDING | APPROVED | REJECTED`
- availability: `PRESENCIAL | VIRTUAL | AMBOS`
- createdAt
- updatedAt

## OfferedService

Tabla `offered_services`.

- id
- professionalId
- categoryId
- name
- description (nullable)
- price
- currency (por defecto `PYG`)
- active

## ServiceRequest

Tabla `service_requests`. Entidad única del concepto: no existen vistas ni entidades paralelas sobre esta tabla.

- id
- professionalId
- clientName
- clientEmail
- clientPhone (nullable)
- subject
- description (máx. 2000 caracteres)
- desiredDate (nullable)
- status: `PENDING | ACCEPTED | REJECTED`
- createdAt
- updatedAt

La solicitud guarda los datos de contacto informados en el formulario. El cliente autenticado consulta su historial por coincidencia de `clientEmail` con el email de su cuenta.

**Deuda conocida:** la creación de solicitudes es pública (formulario abierto en el perfil del profesional), por lo que no existe clave foránea a `users`. Mientras la creación no exija sesión, la pertenencia por email no es verificable de forma fuerte. Cerrar esta deuda implica una migración que añada `client_id` y exigir autenticación en el `POST`.

## Estados de la solicitud

```
PENDING ──accept──> ACCEPTED   (terminal)
        ──reject──> REJECTED   (terminal)
```

Cualquier transición desde un estado terminal, o hacia un estado no contemplado, se rechaza con HTTP 409. `COMPLETED` y `CANCELLED` no forman parte del MVP.

## Reglas principales

1. Un usuario tiene exactamente un rol.
2. Nunca se almacenan contraseñas sin hash.
3. Una cuenta inactiva no puede autenticarse ni operar.
4. Un profesional solo es visible en el catálogo si está `APPROVED`, su usuario está activo y tiene al menos un servicio activo.
5. Un profesional rechazado no puede tener servicios visibles.
6. `approvalStatus` solo transiciona desde `PENDING`, y únicamente por acción de un ADMIN.
7. Un usuario profesional tiene como máximo un perfil profesional.
8. Una solicitud solo puede crearse contra un profesional activo y aprobado.
9. Toda solicitud nace en `PENDING`; cada cambio de estado registra `updatedAt`.
10. Solo el profesional destinatario lista, consulta y resuelve sus solicitudes.
11. Solo el cliente propietario consulta su solicitud.
12. Los datos de contacto del profesional (teléfono y WhatsApp) no se exponen en el catálogo público: se entregan al cliente cuando su solicitud está `ACCEPTED`.
13. Las categorías se crean siempre con `active = true`.
14. Los precios llevan moneda explícita.
