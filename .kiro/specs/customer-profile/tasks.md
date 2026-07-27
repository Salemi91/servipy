# Plan de Implementación: Perfil de Cliente

## Resumen

Implementación del vertical slice `client` para gestión del perfil de cliente en ServiPy. Incluye migración de base de datos, endpoints REST (consultar perfil, editar datos, subir foto, cambiar contraseña, historial de solicitudes), y feature module frontend en Angular con formularios reactivos, carga de foto, y listado paginado de solicitudes.

## Tareas

- [x] 1. Migración de base de datos y entidades de dominio
  - [x] 1.1 Crear migración Flyway `V3__add_client_profile_fields.sql`
    - Agregar columnas `phone VARCHAR(20) NULL` y `photo_url VARCHAR(500) NULL` a la tabla `users`
    - Crear tabla `service_requests` con campos id, client_id, professional_id, service_name, professional_name, status (ENUM), created_at, updated_at
    - Crear índices `idx_requests_client_status` e `idx_requests_client_created`
    - Agregar foreign keys a `users.id`
    - _Requisitos: 2.1, 3.1, 5.1_

  - [x] 1.2 Actualizar entidad `User` y crear entidad `ServiceRequest`
    - Agregar campos `phone` y `photoUrl` a la entidad JPA `User` existente
    - Crear entidad JPA `ServiceRequest` en `py.com.servipy.client.domain`
    - Crear enum `RequestStatus` con valores PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED
    - _Requisitos: 5.1, 2.1, 3.1_

  - [x] 1.3 Crear repositorios para el slice `client`
    - Crear `ServiceRequestRepository` extendiendo `JpaRepository` y `JpaSpecificationExecutor`
    - Crear `ServiceRequestSpecification` para filtrado dinámico por client_id y status
    - _Requisitos: 5.2, 5.7_

- [x] 2. Implementar endpoint de consulta de perfil
  - [x] 2.1 Crear DTOs y servicio de perfil del cliente
    - Crear record `ClientProfileResponse` con campos id, name, email, phone, photoUrl, updatedAt
    - Crear interfaz `ClientProfileService` y su implementación
    - Implementar método `getProfile(Long userId)` que obtiene datos del usuario autenticado
    - Utilizar DTO para la respuesta, sin exponer la entidad JPA directamente
    - _Requisitos: 1.1, 1.4, 1.5_

  - [x] 2.2 Crear `ClientProfileController` con endpoint GET `/api/v1/client/profile`
    - Aplicar `@PreAuthorize("hasRole('CLIENT')")` al controlador
    - Extraer userId del `SecurityContext`
    - Retornar HTTP 200 con `ClientProfileResponse`
    - Manejar casos de usuario no encontrado (404)
    - _Requisitos: 1.1, 1.2, 1.3, 1.4_

  - [ ]* 2.3 Escribir tests unitarios para consulta de perfil
    - Test de respuesta exitosa con datos completos
    - Test de 401 sin token, 403 con rol incorrecto, 404 perfil no encontrado
    - _Requisitos: 1.1, 1.2, 1.3, 1.4_

- [x] 3. Implementar endpoint de edición de datos personales
  - [x] 3.1 Crear DTO `ClientProfileUpdateRequest` con validaciones
    - Campo `name`: @NotBlank, @Size(min=2, max=100), trimming de espacios
    - Campo `phone`: @Pattern para caracteres permitidos (dígitos, espacios, guiones, +), @Size(min=7, max=20), nullable
    - _Requisitos: 2.2, 2.3, 2.7_

  - [x] 3.2 Implementar método `updateProfile` en `ClientProfileService`
    - Actualizar name y phone del usuario
    - Ignorar campos email, role, id aunque vengan en el body
    - Permitir phone null/ausente (almacenar como null)
    - Actualizar campo updatedAt
    - _Requisitos: 2.1, 2.6, 2.9_

  - [x] 3.3 Crear endpoint PUT `/api/v1/client/profile` en el controlador
    - Validar body con @Valid y manejar BindingResult
    - Retornar HTTP 200 con perfil actualizado
    - Retornar HTTP 400 con Error_Uniforme para validaciones fallidas
    - _Requisitos: 2.1, 2.2, 2.3, 2.4, 2.5, 2.7, 2.8_

  - [ ]* 3.4 Escribir property test para round-trip de actualización de perfil
    - **Property 1: Round-trip de actualización de perfil**
    - **Valida: Requisitos 2.1**

  - [ ]* 3.5 Escribir property test para rechazo de nombres inválidos
    - **Property 2: Rechazo de nombres inválidos**
    - **Valida: Requisitos 2.2**

  - [ ]* 3.6 Escribir property test para rechazo de teléfonos inválidos
    - **Property 3: Rechazo de teléfonos inválidos**
    - **Valida: Requisitos 2.3, 2.7**

  - [ ]* 3.7 Escribir property test para inmutabilidad de campos protegidos
    - **Property 4: Inmutabilidad de campos protegidos**
    - **Valida: Requisitos 2.6**

- [x] 4. Implementar endpoint de cambio de foto de perfil
  - [x] 4.1 Crear `LocalPhotoStorageService` para almacenamiento de imágenes
    - Implementar interfaz `PhotoStorageService`
    - Validar MIME type por magic bytes (JPEG: FFD8FF, PNG: 89504E47, WebP: RIFF...WEBP)
    - Almacenar archivo en directorio configurable con UUID en el nombre
    - Retornar URL pública de acceso
    - _Requisitos: 3.1, 3.2, 3.8_

  - [x] 4.2 Crear excepciones personalizadas y handlers
    - Crear `InvalidFileTypeException`, `FileTooLargeException`, `PhotoStorageException`
    - Registrar handlers en `GlobalExceptionHandler` que retornan Error_Uniforme
    - _Requisitos: 3.3, 3.4, 3.9_

  - [x] 4.3 Crear endpoint PUT `/api/v1/client/profile/photo`
    - Recibir `MultipartFile` en request
    - Validar tamaño máximo 5 MB y archivo no vacío (0 bytes)
    - Delegar validación de MIME a `PhotoStorageService`
    - Actualizar `photoUrl` y `updatedAt` del usuario
    - Retornar HTTP 200 con `PhotoUploadResponse`
    - _Requisitos: 3.1, 3.3, 3.4, 3.5, 3.6, 3.7, 3.9, 3.10_

  - [ ]* 4.4 Escribir property test para validación de MIME por contenido real
    - **Property 5: Validación de MIME por contenido real**
    - **Valida: Requisitos 3.2, 3.3**

- [x] 5. Checkpoint — Verificar que todos los tests pasan
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Implementar endpoint de cambio de contraseña
  - [x] 6.1 Crear DTO `PasswordChangeRequest` con validaciones
    - Campo `currentPassword`: @NotBlank
    - Campo `newPassword`: @NotBlank, @Size(min=8, max=72), validación de complejidad (mayúscula + minúscula + dígito)
    - _Requisitos: 4.3, 4.4, 4.8_

  - [x] 6.2 Implementar método `changePassword` en `ClientProfileService`
    - Verificar currentPassword contra hash almacenado con bcrypt
    - Validar que newPassword sea diferente de currentPassword
    - Generar nuevo hash bcrypt y actualizar en BD
    - Actualizar updatedAt
    - No incluir hash en ninguna respuesta
    - _Requisitos: 4.1, 4.2, 4.7, 4.9_

  - [x] 6.3 Crear endpoint PUT `/api/v1/client/profile/password`
    - Validar body con @Valid
    - Manejar `InvalidCurrentPasswordException` con HTTP 400 y code `INVALID_CURRENT_PASSWORD`
    - Retornar HTTP 200 con mensaje de éxito
    - _Requisitos: 4.1, 4.2, 4.5, 4.6_

  - [ ]* 6.4 Escribir property test para round-trip de contraseña
    - **Property 6: Round-trip de contraseña**
    - **Valida: Requisitos 4.1**

  - [ ]* 6.5 Escribir property test para rechazo de contraseñas con complejidad insuficiente
    - **Property 7: Rechazo de contraseñas con complejidad insuficiente**
    - **Valida: Requisitos 4.3, 4.8**

  - [ ]* 6.6 Escribir property test para contraseña nueva distinta a la actual
    - **Property 8: Contraseña nueva distinta a la actual**
    - **Valida: Requisitos 4.9**

  - [ ]* 6.7 Escribir property test para ausencia de hash en respuestas
    - **Property 9: Ausencia de hash en respuestas**
    - **Valida: Requisitos 4.7**

- [x] 7. Implementar endpoint de historial de solicitudes
  - [x] 7.1 Crear DTOs y servicio de historial de solicitudes
    - Crear records `ServiceRequestResponse` y `ServiceRequestPageResponse`
    - Crear interfaz `ClientRequestService` y su implementación
    - Implementar consulta paginada con filtro por status usando `Specification`
    - Ordenar por createdAt descendente
    - _Requisitos: 5.1, 5.2, 5.3, 5.7, 5.9_

  - [x] 7.2 Crear `ClientRequestController` con endpoint GET `/api/v1/client/requests`
    - Recibir parámetros opcionales: status, page (default 0), size (default 20, max 100)
    - Validar parámetro status contra valores válidos de `RequestStatus`
    - Validar rangos de page y size
    - Retornar HTTP 200 con respuesta paginada
    - _Requisitos: 5.1, 5.4, 5.5, 5.6, 5.7, 5.8, 5.9, 5.10_

  - [ ]* 7.3 Escribir property test para aislamiento de solicitudes por cliente
    - **Property 10: Aislamiento de solicitudes por cliente**
    - **Valida: Requisitos 5.2**

  - [ ]* 7.4 Escribir property test para ordenamiento descendente por fecha
    - **Property 11: Ordenamiento descendente por fecha**
    - **Valida: Requisitos 5.3**

  - [ ]* 7.5 Escribir property test para filtrado por estado correcto
    - **Property 12: Filtrado por estado correcto**
    - **Valida: Requisitos 5.7**

  - [ ]* 7.6 Escribir property test para consistencia de paginación
    - **Property 13: Consistencia de paginación**
    - **Valida: Requisitos 5.9**

- [x] 8. Checkpoint — Verificar backend completo
  - Ensure all tests pass, ask the user if questions arise.

- [x] 9. Implementar modelos y servicios del frontend
  - [x] 9.1 Crear modelos TypeScript para el feature client
    - Crear `client-profile.model.ts` con interfaces ClientProfile, ProfileUpdateRequest, PasswordChangeRequest, PhotoUploadResponse
    - Crear `service-request.model.ts` con interfaces ServiceRequest y PaginatedResponse<T>
    - _Requisitos: 6.1, 7.1_

  - [x] 9.2 Crear `ClientProfileService` en el frontend
    - Implementar métodos: getProfile(), updateProfile(), uploadPhoto(), changePassword()
    - Usar el patrón de `ApiService` existente en el proyecto
    - Para uploadPhoto usar HttpClient directamente con FormData
    - _Requisitos: 6.1, 6.2, 6.3, 6.5_

  - [x] 9.3 Crear `ClientRequestService` en el frontend
    - Implementar método getRequests(status?, page?, size?) con parámetros opcionales
    - Retornar Observable<PaginatedResponse<ServiceRequest>>
    - _Requisitos: 7.1, 7.3_

- [x] 10. Implementar página de perfil del cliente
  - [x] 10.1 Crear `ProfilePageComponent` como contenedor principal
    - Mostrar datos actuales del cliente (nombre, email, teléfono, foto)
    - Implementar estado de carga mientras se obtienen los datos
    - Tiempo de carga máximo 2 segundos con indicador visual
    - _Requisitos: 6.1_

  - [x] 10.2 Crear `ProfileEditFormComponent` con Reactive Forms
    - Campo name: required, minLength(2), maxLength(100)
    - Campo phone: optional, maxLength(20), pattern para caracteres permitidos
    - Mostrar mensajes de validación en tiempo real al perder foco (< 300ms)
    - Deshabilitar botón de envío durante request HTTP
    - Mostrar notificación de éxito/error según respuesta
    - _Requisitos: 6.2, 6.6, 6.7, 6.8_

  - [x] 10.3 Crear `PhotoUploadComponent`
    - Input de tipo file con accept para JPEG, PNG y WebP
    - Validar tipo y tamaño (max 5 MB) antes de enviar
    - Mostrar preview de la imagen seleccionada
    - Mostrar error si tipo o tamaño no son válidos sin enviar al backend
    - Deshabilitar botón durante upload
    - _Requisitos: 6.3, 6.4, 6.7_

  - [x] 10.4 Crear `PasswordChangeComponent` con Reactive Forms
    - Campos: contraseña actual (required), nueva contraseña (required, min 8, max 72 chars)
    - Deshabilitar botón de envío hasta que validaciones pasen
    - Deshabilitar botón durante request HTTP
    - Mostrar notificación de éxito/error
    - _Requisitos: 6.5, 6.6, 6.7, 6.8_

- [x] 11. Implementar página de historial de solicitudes
  - [x] 11.1 Crear `RequestHistoryPageComponent` con listado paginado
    - Mostrar lista de solicitudes ordenada por fecha descendente
    - Mostrar para cada solicitud: servicio, profesional, estado (badge con color), fechas formateadas dd/MM/yyyy HH:mm
    - Indicador de carga mientras se obtienen datos
    - Mensaje de lista vacía cuando no hay solicitudes
    - Timeout de 30 segundos con mensaje de error y opción de reintento
    - _Requisitos: 7.1, 7.2, 7.4, 7.5, 7.6_

  - [x] 11.2 Crear `RequestStatusBadgeComponent` con colores por estado
    - Componente standalone que recibe status como input
    - Colores diferenciados por cada valor de Estado_de_Solicitud usando Tailwind
    - _Requisitos: 7.2_

  - [x] 11.3 Implementar filtro por estado y paginación
    - Dropdown/select con opciones: Todas, PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED
    - Al cambiar filtro, recargar lista en máximo 1 segundo
    - Deshabilitar filtro durante carga
    - Paginación con máximo 50 elementos por página
    - _Requisitos: 7.3, 7.5_

- [x] 12. Configurar rutas y guards del feature client
  - [x] 12.1 Actualizar `routes.ts` del feature client
    - Definir rutas: `/client/profile` y `/client/requests`
    - Aplicar `authGuard` para redirigir a login si no autenticado
    - Configurar redirección por defecto a profile
    - _Requisitos: 6.10, 7.7_

  - [x] 12.2 Configurar timeout de 15 segundos en servicios HTTP
    - Agregar operator `timeout(15000)` en las llamadas del ClientProfileService
    - Restaurar estado de botón en caso de timeout
    - Mostrar notificación de error de timeout
    - _Requisitos: 6.9_

- [x] 13. Checkpoint final — Verificar implementación completa
  - Ensure all tests pass, ask the user if questions arise.

## Notas

- Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido
- Cada tarea referencia requisitos específicos para trazabilidad
- Los checkpoints aseguran validación incremental
- Los property tests validan propiedades universales de corrección usando jqwik
- Los unit tests validan ejemplos específicos y edge cases con JUnit 5
- La migración Flyway sigue la convención existente: `V3__` (las existentes son V1 y V2)
- El frontend extiende el feature module `client` que ya tiene placeholder y routes.ts

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2", "9.1"] },
    { "id": 2, "tasks": ["1.3", "2.1", "9.2", "9.3"] },
    { "id": 3, "tasks": ["2.2", "3.1", "4.1", "6.1", "7.1"] },
    { "id": 4, "tasks": ["2.3", "3.2", "4.2", "6.2", "7.2"] },
    { "id": 5, "tasks": ["3.3", "4.3", "6.3", "10.1", "11.1"] },
    { "id": 6, "tasks": ["3.4", "3.5", "3.6", "3.7", "4.4", "6.4", "6.5", "6.6", "6.7", "7.3", "7.4", "7.5", "7.6", "10.2", "10.3", "10.4", "11.2"] },
    { "id": 7, "tasks": ["11.3", "12.1", "12.2"] }
  ]
}
```
