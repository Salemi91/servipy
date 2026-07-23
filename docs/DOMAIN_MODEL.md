# Modelo de dominio mínimo

## User

- id
- name
- email
- passwordHash
- role: `CLIENT | PROFESSIONAL | ADMIN`
- active
- createdAt
- updatedAt

## Country

- id
- name
- code
- defaultCurrency

## City

- id
- countryId
- name
- latitude
- longitude

## Category

- id
- name
- icon
- description
- active

## ProfessionalProfile

- id
- userId
- photoUrl
- phone
- whatsapp
- description
- cityId
- approvalStatus
- availability
- createdAt
- updatedAt

## OfferedService

- id
- professionalId
- categoryId
- name
- description
- price
- currency
- active

## ServiceRequest

- id
- clientId
- professionalId
- offeredServiceId
- description
- address
- contactPhone
- preferredDate
- scheduledDate
- status
- createdAt
- updatedAt

## Reglas principales

1. Un usuario tiene exactamente un rol.
2. Un profesional solo es visible en búsquedas cuando está `APPROVED`.
3. Un profesional rechazado no puede publicar servicios visibles.
4. Solo el cliente propietario consulta o cancela su solicitud.
5. Solo el profesional destinatario acepta o completa la solicitud.
6. WhatsApp se habilita cuando la solicitud está `ACCEPTED`.
7. No almacenar contraseñas sin hash.
