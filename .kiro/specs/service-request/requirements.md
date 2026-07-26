# Requirements Document

## Introduction

Solicitudes de servicio para ServiPy. Esta feature permite a un cliente enviar una solicitud de servicio a un profesional desde su perfil público, y al profesional consultar y gestionar el estado de las solicitudes recibidas. El flujo cubre desde la creación de la solicitud con validación de datos, hasta la transición de estados (PENDING → ACCEPTED / REJECTED) por parte del profesional. Durante el MVP, el acceso al panel del profesional se resuelve mediante un identificador demo o ruta temporal documentada, sin implementar JWT.

## Glossary

- **Service_Request_API**: Conjunto de endpoints REST bajo `/api/v1` que gestionan la creación, consulta y cambio de estado de solicitudes de servicio.
- **Service_Request**: Entidad que representa una solicitud enviada por un cliente a un profesional. Contiene nombre del cliente, correo, teléfono (opcional), asunto, descripción, fecha deseada (opcional), estado, profesional asociado, identificador único, fecha de creación y fecha de actualización.
- **Request_Status**: Estado de una solicitud de servicio. Valores posibles: `PENDING`, `ACCEPTED`, `REJECTED`.
- **Client**: Persona no autenticada que envía una solicitud de servicio a un profesional desde el perfil público.
- **Professional**: Usuario con perfil profesional aprobado y activo que recibe y gestiona solicitudes de servicio.
- **Active_Professional**: Profesional cuyo usuario tiene `active = true` y cuyo perfil tiene `approvalStatus = APPROVED`.
- **Service_Request_Frontend**: Módulo Angular del frontend responsable del formulario de solicitud (lado cliente) y del panel de gestión (lado profesional).
- **Confirmation_View**: Vista que se muestra al cliente tras la creación exitosa de una solicitud, incluyendo el identificador asignado.
- **Description_Max_Length**: Longitud máxima permitida para el campo descripción de una solicitud (2000 caracteres).

## Requirements

### Requirement 1: Crear solicitud

**User Story:** Como cliente, quiero enviar una solicitud a un profesional, para explicarle el servicio que necesito.

#### Acceptance Criteria

1. WHEN a Client submits a service request specifying a professional identifier, THE Service_Request_API SHALL validate that the professional exists, has `active = true` on the associated User, and has `approvalStatus = APPROVED` on the ProfessionalProfile before creating the Service_Request.
2. THE Service_Request_API SHALL require the fields name, email, subject, and description as mandatory in every service request creation payload.
3. WHEN a Client provides an email field, THE Service_Request_API SHALL validate that the value conforms to a standard email format (RFC 5322 simplified).
4. IF a Client submits a description that is empty or exceeds Description_Max_Length (2000 characters), THEN THE Service_Request_API SHALL reject the request with HTTP 400 and an error message identifying the description field.
5. WHEN a Service_Request is created successfully, THE Service_Request_API SHALL assign the initial Request_Status as `PENDING`.
6. WHEN a Service_Request is created successfully, THE Service_Request_API SHALL assign a unique identifier and record the creation timestamp.
7. IF the specified professional identifier does not correspond to an Active_Professional or does not exist, THEN THE Service_Request_API SHALL return HTTP 404 with an error response indicating the professional was not found.
8. IF the submitted payload contains invalid or missing required fields, THEN THE Service_Request_API SHALL return HTTP 400 with an error response listing each field that failed validation and the corresponding reason.

### Requirement 2: Confirmación

**User Story:** Como cliente, quiero recibir una confirmación tras enviar mi solicitud, para saber que fue registrada correctamente.

#### Acceptance Criteria

1. WHEN a Service_Request is created successfully, THE Service_Request_API SHALL return HTTP 201 with a response body containing at minimum the assigned identifier of the new Service_Request.
2. WHEN the Service_Request_Frontend receives a successful creation response, THE Service_Request_Frontend SHALL display the Confirmation_View including the identifier of the created Service_Request.
3. WHILE a service request creation HTTP request is in progress, THE Service_Request_Frontend SHALL disable the submit button to prevent duplicate submissions from double-click or repeated form submission.

### Requirement 3: Consulta por profesional

**User Story:** Como profesional, quiero ver las solicitudes que he recibido, para poder revisarlas y decidir si las acepto.

#### Acceptance Criteria

1. WHEN a Professional requests their received service requests, THE Service_Request_API SHALL return only Service_Request entities associated with that professional's identifier.
2. WHEN a Professional provides a status query parameter, THE Service_Request_API SHALL return only Service_Request entities matching the specified Request_Status value.
3. THE Service_Request_API SHALL include in each list item: client name, subject, Request_Status, and creation timestamp.
4. THE Service_Request_API SHALL NOT return Service_Request entities belonging to a different professional in the response for a given professional's query.

### Requirement 4: Detalle de solicitud

**User Story:** Como profesional, quiero ver el contenido completo de una solicitud, para entender la necesidad del cliente.

#### Acceptance Criteria

1. WHEN a Professional requests the detail of a Service_Request by identifier, THE Service_Request_API SHALL return the full content of the Service_Request including: identifier, client name, email, phone (or null), subject, description, desired date (or null), Request_Status, creation timestamp, and update timestamp.
2. IF the requested Service_Request identifier does not exist, THEN THE Service_Request_API SHALL return HTTP 404 with an error response indicating the service request was not found.
3. THE Service_Request_API SHALL NOT expose internal technical identifiers (such as database primary keys of related entities) or server-side metadata not relevant to the professional in the detail response.

### Requirement 5: Cambio de estado

**User Story:** Como profesional, quiero aceptar o rechazar solicitudes, para comunicar mi decisión al cliente.

#### Acceptance Criteria

1. WHEN a Professional requests a status change on a Service_Request with current status `PENDING` and the target status is `ACCEPTED`, THE Service_Request_API SHALL update the Request_Status to `ACCEPTED`.
2. WHEN a Professional requests a status change on a Service_Request with current status `PENDING` and the target status is `REJECTED`, THE Service_Request_API SHALL update the Request_Status to `REJECTED`.
3. IF a Professional requests a status change on a Service_Request whose current status is `ACCEPTED` or `REJECTED`, THEN THE Service_Request_API SHALL return HTTP 409 Conflict with an error response indicating the transition is not allowed.
4. IF a Professional requests a status transition that is not a valid combination (e.g., from `PENDING` to an undefined status), THEN THE Service_Request_API SHALL return HTTP 409 Conflict with an error response indicating the transition is invalid.
5. WHEN a status change is applied successfully, THE Service_Request_API SHALL record the update timestamp on the modified Service_Request.
