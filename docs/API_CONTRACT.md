# Contrato de API — ServiPy

El contrato vigente vive en **[`PROJECT.md` § 2.4 Contrato de API](../PROJECT.md#24-contrato-de-api)**, junto con las reglas de seguridad (§ 2.5) y la tabla de códigos de error (§ 3.4).

Este archivo se mantiene solo como puntero para no romper enlaces existentes. No se documentan endpoints aquí: duplicarlos generaría una segunda fuente de verdad que se desincroniza.

Reglas invariantes del contrato:

- Prefijo `/api/v1`, JSON en request y response, fechas ISO 8601.
- JWT en `Authorization: Bearer <token>`.
- Nunca se serializan entidades JPA: siempre DTOs.
- Todo error usa la estructura uniforme `{ timestamp, status, code, message, errors[] }`.
- Ninguna ruta ni campo cambia sin acuerdo explícito entre frontend y backend.
