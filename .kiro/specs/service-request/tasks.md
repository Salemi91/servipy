# Implementation Plan: Service Request

## Overview

Implementación del módulo de solicitudes de servicio para ServiPy. Cubre la creación de la entidad `ServiceRequest`, los endpoints REST anidados bajo `/api/v1/professionals/{professionalId}/service-requests`, y los componentes Angular para el formulario público y el panel de gestión del profesional. Stack: Java 21 + Spring Boot 3 (backend), Angular 17+ (frontend), MySQL 8 (persistencia).

## Tasks

- [x] 1. Validar dependencias y contrato
  - [x] 1.1 Confirmar existencia del profesional
    - Verificar que `ProfessionalProfileRepository` tiene método `findById` y que la entidad `ProfessionalProfile` expone `getUser().getActive()` y `getApprovalStatus()`
    - Confirmar que `ApprovalStatus.APPROVED` existe en el enum actual
    - _Requirements: 1.1, 1.7_
  - [x] 1.2 Confirmar endpoints y payloads
    - Revisar `SecurityConfig.java` para confirmar los matchers actuales y planificar las adiciones necesarias
    - Verificar estructura de `ErrorResponse` y `GlobalExceptionHandler` para reutilización
    - _Requirements: 3, 4, 5_
  - [x] 1.3 Documentar mecanismo temporal de acceso del profesional
    - Agregar comentario en `SecurityConfig.java` indicando que los endpoints de service-requests son públicos en MVP
    - Agregar `// TODO: Proteger con JWT cuando se implemente autenticación completa`
    - _Requirements: 5_

- [x] 2. Crear persistencia
  - [x] 2.1 Crear migración de `service_request`
    - Crear archivo `backend/src/main/resources/db/migration/V3__service_requests_table.sql`
    - Definir tabla `service_requests` con columnas: id, professional_id, client_name, client_email, client_phone, subject, description, desired_date, status (ENUM), created_at, updated_at
    - Crear índices: idx_requests_professional, idx_requests_status, idx_requests_professional_status, idx_requests_created_at
    - _Requirements: 1.5, 1.6_
  - [x] 2.2 Crear enum de estados
    - Crear `backend/src/main/java/py/com/servipy/servicerequest/domain/RequestStatus.java`
    - Implementar valores: PENDING, ACCEPTED, REJECTED
    - Implementar método `canTransitionTo(RequestStatus target)` — solo PENDING puede transicionar a ACCEPTED o REJECTED
    - _Requirements: 5.1, 5.2, 5.3, 5.4_
  - [x] 2.3 Definir restricciones y relaciones
    - Crear `backend/src/main/java/py/com/servipy/servicerequest/domain/ServiceRequest.java` — entidad JPA con `@ManyToOne` a `ProfessionalProfile`
    - Columnas NOT NULL: professional_id, client_name, client_email, subject, description, status, created_at, updated_at
    - Columnas opcionales: client_phone, desired_date
    - FK constraint hacia `professional_profiles(id)`
    - _Requirements: 1.2, 1.5, 1.6_

- [x] 3. Implementar creación de solicitud
  - [x] 3.1 Crear comando y validaciones
    - Crear `backend/src/main/java/py/com/servipy/servicerequest/application/dto/CreateServiceRequestDto.java` — record con Jakarta Bean Validation
    - Validaciones: `@NotBlank` en name, email, subject, description; `@Email` en email; `@Size(max=150)` en name; `@Size(max=200)` en subject; `@Size(max=2000)` en description; `@Size(max=20)` en phone
    - Crear `backend/src/main/java/py/com/servipy/servicerequest/application/dto/CreateServiceRequestResponse.java` — record con campo `id`
    - _Requirements: 1.2, 1.3, 1.4, 1.8_
  - [x] 3.2 Crear caso de uso
    - Crear `backend/src/main/java/py/com/servipy/servicerequest/application/ServiceRequestService.java`
    - Implementar método `create(Long professionalId, CreateServiceRequestDto dto)` que busca profesional activo, construye entidad con status=PENDING, persiste y retorna response con id
    - _Requirements: 1.1, 1.5, 1.6_
  - [x] 3.3 Validar profesional activo y aprobado
    - Implementar método privado `findActiveProfessional(Long professionalId)` en el servicio
    - Filtrar por `user.active == true` AND `approvalStatus == APPROVED`
    - Lanzar `ResourceNotFoundException("Profesional no encontrado")` si no cumple condiciones
    - _Requirements: 1.1, 1.7_
  - [x] 3.4 Persistir solicitud como `PENDING`
    - Crear `backend/src/main/java/py/com/servipy/servicerequest/infrastructure/persistence/ServiceRequestRepository.java` — extiende JpaRepository
    - Definir queries: `findByProfessionalIdOrderByCreatedAtDesc`, `findByProfessionalIdAndStatusOrderByCreatedAtDesc`, `findByIdAndProfessionalId`
    - _Requirements: 1.5, 1.6_
  - [x] 3.5 Crear endpoint POST
    - Crear `backend/src/main/java/py/com/servipy/servicerequest/infrastructure/web/ServiceRequestController.java`
    - Implementar `POST /api/v1/professionals/{professionalId}/service-requests` — recibe `@Valid @RequestBody CreateServiceRequestDto`, retorna 201 con `CreateServiceRequestResponse`
    - Crear `backend/src/main/java/py/com/servipy/shared/exception/InvalidStateTransitionException.java`
    - Modificar `GlobalExceptionHandler.java` — agregar handlers para `MethodArgumentNotValidException` (400) e `InvalidStateTransitionException` (409)
    - Modificar `SecurityConfig.java` — agregar `.requestMatchers("/api/v1/professionals/*/service-requests/**").permitAll()`
    - _Requirements: 1.8, 2.1_

- [x] 4. Implementar consulta de solicitudes
  - [x] 4.1 Crear listado por profesional
    - Implementar método `findByProfessional(Long professionalId, RequestStatus status)` en `ServiceRequestService`
    - Crear `backend/src/main/java/py/com/servipy/servicerequest/application/dto/ServiceRequestSummaryDto.java` — record con id, clientName, subject, status, createdAt
    - Mapear entidades a DTOs de summary
    - _Requirements: 3.1, 3.3, 3.4_
  - [x] 4.2 Implementar filtro por estado
    - Si el parámetro `status` es proporcionado, usar `findByProfessionalIdAndStatusOrderByCreatedAtDesc`
    - Si no se proporciona, usar `findByProfessionalIdOrderByCreatedAtDesc` para devolver todas
    - _Requirements: 3.2_
  - [x] 4.3 Crear consulta de detalle
    - Implementar método `findDetail(Long professionalId, Long requestId)` en `ServiceRequestService`
    - Crear `backend/src/main/java/py/com/servipy/servicerequest/application/dto/ServiceRequestDetailDto.java` — record con todos los campos del detalle (sin FK internas)
    - Lanzar `ResourceNotFoundException` si no existe o no pertenece al profesional
    - _Requirements: 4.1, 4.2, 4.3_
  - [x] 4.4 Crear endpoints GET
    - Implementar `GET /api/v1/professionals/{professionalId}/service-requests` con `@RequestParam(required = false) RequestStatus status`
    - Implementar `GET /api/v1/professionals/{professionalId}/service-requests/{requestId}`
    - _Requirements: 3.1, 3.2, 4.1_

- [x] 5. Implementar cambio de estado
  - [x] 5.1 Definir regla de transición
    - Utilizar `RequestStatus.canTransitionTo(target)` — solo PENDING permite transición a ACCEPTED o REJECTED
    - Estados terminales (ACCEPTED, REJECTED) rechazan cualquier transición
    - _Requirements: 5.1, 5.2, 5.3, 5.4_
  - [x] 5.2 Implementar caso de uso
    - Implementar método `changeStatus(Long professionalId, Long requestId, RequestStatus targetStatus)` en `ServiceRequestService`
    - Crear `backend/src/main/java/py/com/servipy/servicerequest/application/dto/ChangeStatusDto.java` — record con `@NotNull RequestStatus status`
    - Actualizar `status` y `updatedAt` en la entidad
    - _Requirements: 5.1, 5.2, 5.5_
  - [x] 5.3 Responder `409` ante transición inválida
    - Lanzar `InvalidStateTransitionException` con mensaje descriptivo cuando `canTransitionTo` retorna false
    - `GlobalExceptionHandler` ya registrado en tarea 3.5 intercepta y retorna 409 con `ErrorResponse`
    - _Requirements: 5.3, 5.4_
  - [x] 5.4 Crear endpoint PATCH
    - Implementar `PATCH /api/v1/professionals/{professionalId}/service-requests/{requestId}/status`
    - Recibe `@Valid @RequestBody ChangeStatusDto`, delega a `serviceRequestService.changeStatus()`
    - Retorna 200 OK (sin body)
    - _Requirements: 5.1, 5.2, 5.3_

- [ ] 6. Probar backend
  - [ ]* 6.1 Probar solicitud válida
    - Crear `backend/src/test/java/py/com/servipy/servicerequest/application/ServiceRequestServiceTest.java`
    - Test: `should_createRequest_when_professionalIsActiveAndApproved` — verificar que retorna id y status=PENDING
    - _Requirements: 1.1, 1.5, 2.1_
  - [ ]* 6.2 Probar campos inválidos
    - Crear `backend/src/test/java/py/com/servipy/servicerequest/infrastructure/web/ServiceRequestControllerTest.java`
    - Tests: `should_return400_when_nameMissing`, `should_return400_when_emailInvalid`, `should_return400_when_descriptionTooLong`
    - _Requirements: 1.2, 1.3, 1.4, 1.8_
  - [ ]* 6.3 Probar profesional inexistente
    - Test: `should_throw404_when_professionalNotFound`, `should_throw404_when_professionalNotActive`, `should_throw404_when_professionalNotApproved`
    - _Requirements: 1.7_
  - [ ]* 6.4 Probar listado por profesional
    - Test: `should_returnOnlyOwnRequests_when_listByProfessional`
    - _Requirements: 3.1, 3.4_
  - [ ]* 6.5 Probar detalle
    - Tests: `should_returnDetail_when_requestExistsForProfessional`, `should_throw404_when_requestNotFound`, `should_throw404_when_requestBelongsToOtherProfessional`
    - _Requirements: 4.1, 4.2_
  - [ ]* 6.6 Probar aceptación
    - Test: `should_acceptRequest_when_statusIsPending`
    - _Requirements: 5.1_
  - [ ]* 6.7 Probar rechazo
    - Test: `should_rejectRequest_when_statusIsPending`
    - _Requirements: 5.2_
  - [ ]* 6.8 Probar transición inválida
    - Tests: `should_throw409_when_statusIsAccepted`, `should_throw409_when_statusIsRejected`
    - _Requirements: 5.3_

- [x] 7. Implementar formulario Angular
  - [x] 7.1 Crear modelos TypeScript
    - Crear `frontend/src/app/shared/models/service-request.model.ts`
    - Interfaces: `CreateServiceRequestPayload`, `CreateServiceRequestResponse`, `ServiceRequestSummary`, `ServiceRequestDetail`, `ChangeStatusPayload`
    - _Requirements: 1.2, 3.3, 4.1_
  - [x] 7.2 Crear servicio HTTP
    - Crear `frontend/src/app/features/professional/requests/services/service-request.service.ts`
    - Métodos: `create()`, `getByProfessional()`, `getDetail()`, `changeStatus()`
    - Inyectar `ApiService` existente para prefijo `/api/v1`
    - _Requirements: 1.1, 3.1, 4.1, 5.1_
  - [x] 7.3 Crear formulario reactivo
    - Crear `frontend/src/app/features/public/catalog/catalog-detail/components/service-request-form/service-request-form.component.ts`
    - Crear template HTML con campos: name, email, phone, subject, description, desiredDate
    - Usar `ReactiveFormsModule` con `FormGroup` y `FormControl`
    - _Requirements: 1.2_
  - [x] 7.4 Agregar validaciones por campo
    - Validators: `Validators.required` en name, email, subject, description; `Validators.email` en email; `Validators.maxLength(150)` en name; `Validators.maxLength(200)` en subject; `Validators.maxLength(2000)` en description
    - Mostrar mensajes de error inline bajo cada campo inválido
    - _Requirements: 1.2, 1.3, 1.4_
  - [x] 7.5 Implementar loading, success y error
    - Estado `isSubmitting` que deshabilita el botón durante la petición
    - En éxito: navegar a Confirmation_View mostrando el id devuelto
    - En error 400: mapear `fieldErrors` del servidor a los campos del formulario
    - En error 404: mostrar mensaje "Profesional no disponible"
    - _Requirements: 2.2, 2.3, 1.8_
  - [x] 7.6 Evitar envíos duplicados
    - Deshabilitar botón submit mientras `isSubmitting = true`
    - Re-habilitar si la petición falla
    - _Requirements: 2.3_

- [x] 8. Implementar panel de solicitudes
  - [x] 8.1 Crear listado por profesional
    - Crear `frontend/src/app/features/professional/requests/request-list/request-list.component.ts` y template
    - Configurar rutas en `frontend/src/app/features/professional/requests/requests.routes.ts`
    - Modificar `frontend/src/app/features/professional/routes.ts` para agregar lazy-load del módulo requests
    - Mostrar tabla con columnas: nombre cliente, asunto, estado, fecha
    - _Requirements: 3.1, 3.3_
  - [x] 8.2 Agregar filtro por estado
    - Implementar select/dropdown con opciones: Todos, PENDING, ACCEPTED, REJECTED
    - Al cambiar filtro, re-llamar `serviceRequestService.getByProfessional()` con query param
    - _Requirements: 3.2_
  - [x] 8.3 Crear vista de detalle
    - Crear `frontend/src/app/features/professional/requests/request-detail/request-detail.component.ts` y template
    - Mostrar todos los campos del detalle: nombre, email, teléfono, asunto, descripción, fecha deseada, estado, timestamps
    - _Requirements: 4.1_
  - [x] 8.4 Permitir aceptar o rechazar
    - Mostrar botones "Aceptar" y "Rechazar" solo cuando `status === 'PENDING'`
    - Llamar `serviceRequestService.changeStatus()` con payload correspondiente
    - Manejar 409 mostrando toast/notificación con mensaje del servidor
    - _Requirements: 5.1, 5.2, 5.3_
  - [x] 8.5 Refrescar estado después de actualizar
    - Tras aceptar/rechazar exitosamente, recargar el detalle desde el servidor
    - Ocultar botones de acción si el estado ya no es PENDING
    - Crear `frontend/src/app/features/professional/requests/components/request-confirmation/request-confirmation.component.ts` para la vista de confirmación post-envío
    - _Requirements: 5.1, 5.2, 5.5_

- [x] 9. Integración y validación
  - [x] 9.1 Integrar formulario con API real
    - Verificar que el formulario envía correctamente al endpoint POST
    - Confirmar manejo de respuestas 201, 400, 404
    - _Requirements: 1, 2_
  - [x] 9.2 Integrar panel con API real
    - Verificar que el listado, detalle y cambio de estado funcionan contra el backend
    - Confirmar filtro por estado y aislamiento por profesional
    - _Requirements: 3, 4, 5_
  - [x]* 9.3 Ejecutar pruebas backend
    - Ejecutar `./mvnw test` y confirmar que todos los tests pasan
    - _Requirements: 1, 2, 3, 4, 5_
  - [x]* 9.4 Ejecutar pruebas frontend
    - Ejecutar `ng test --watch=false` y confirmar que todos los tests pasan
    - _Requirements: 1, 2, 3, 4, 5_
  - [x] 9.5 Ejecutar builds
    - Backend: `./mvnw clean package -DskipTests` — confirmar que compila sin errores
    - Frontend: `ng build` — confirmar que compila sin errores
    - _Requirements: 1, 2, 3, 4, 5_
  - [x] 9.6 Validar flujo completo de demo
    - Confirmar flujo: cliente envía solicitud → profesional ve listado → profesional acepta/rechaza
    - Verificar respuestas HTTP correctas en cada paso
    - _Requirements: 1, 2, 3, 4, 5_

## Notes

- Tasks marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido
- Cada task referencia requirements específicos para trazabilidad
- El stack es Java 21 + Spring Boot 3 (backend) y Angular 17+ con standalone components (frontend)
- Tests backend con JUnit 5 + Mockito; tests frontend con Jasmine + Karma
- Los endpoints de service-requests son públicos en MVP (sin JWT). Se documentará el TODO para protegerlos en la iteración de autenticación
- Property-based tests con jqwik están contemplados en el design pero se priorizan unit tests para el hackathon

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "1.3"] },
    { "id": 1, "tasks": ["2.1", "2.2"] },
    { "id": 2, "tasks": ["2.3", "7.1"] },
    { "id": 3, "tasks": ["3.1", "3.4"] },
    { "id": 4, "tasks": ["3.2", "3.3", "7.2"] },
    { "id": 5, "tasks": ["3.5", "4.1", "4.2"] },
    { "id": 6, "tasks": ["4.3", "4.4", "5.1"] },
    { "id": 7, "tasks": ["5.2", "5.3"] },
    { "id": 8, "tasks": ["5.4", "7.3"] },
    { "id": 9, "tasks": ["7.4", "7.5", "7.6"] },
    { "id": 10, "tasks": ["6.1", "6.2", "6.3", "8.1"] },
    { "id": 11, "tasks": ["6.4", "6.5", "8.2", "8.3"] },
    { "id": 12, "tasks": ["6.6", "6.7", "6.8", "8.4"] },
    { "id": 13, "tasks": ["8.5"] },
    { "id": 14, "tasks": ["9.1", "9.2"] },
    { "id": 15, "tasks": ["9.3", "9.4", "9.5"] },
    { "id": 16, "tasks": ["9.6"] }
  ]
}
```

### Tareas de Refactorización y Mejoras Implementadas

- [x] Protección estricta de endpoints de solicitudes de servicio: solo `POST` de creación permanece público; `GET` de lista/detalle y `PATCH` de cambio de estado exigen `@PreAuthorize("hasRole('PROFESSIONAL')")` y verifican que el `professionalId` de la ruta corresponda al perfil del usuario autenticado (404 si no coincide, para no revelar la existencia del recurso).
- [x] Reemplazo del entry point y access denied handler por defecto de Spring Security por `RestAuthenticationEntryPoint` y `RestAccessDeniedHandler`, de modo que un 401 (sin token o token expirado) o un 403 (rol insuficiente) devuelvan la misma estructura `ErrorResponse` que el resto de la API, en lugar de una respuesta vacía.
- [x] Unificación de la entidad `ServiceRequest`: eliminada la proyección `@Subselect` y el enum `RequestStatus` duplicados del slice `client`, junto con `ClientServiceRequestRepository`, `ClientUserRepository` y la `Specification` que operaban sobre esa copia. El slice `client` ahora consume `ServiceRequestService` (del slice `servicerequest`) a través de la vista de lectura `ClientRequestView`, sin repositorios cruzados entre slices.
- [x] Test de integración `ServiceRequestSecurityTest` que verifica contra la aplicación real: 401 sin token en los tres verbos protegidos, 403 cuando un CLIENT intenta acceder, 404 cuando un profesional consulta la bandeja de otro, y que la creación sigue siendo accesible sin autenticación.
