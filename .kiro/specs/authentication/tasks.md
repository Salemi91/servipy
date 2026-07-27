# Plan de Implementación: Autenticación y Autorización

## Resumen

Plan de implementación para la infraestructura completa de autenticación y autorización de ServiPy. Incluye consolidación de la entidad User en backend, configuración CORS, y la implementación completa del frontend de autenticación (servicio, interceptor, guards, páginas de login/registro).

## Tareas

- [ ] 1. Consolidación de entidad User y configuración backend
  - [x] 1.1 Mejorar la entidad canónica `py.com.servipy.user.domain.User`
    - Agregar constructor con argumentos `User(String name, String email, String passwordHash, Role role)`
    - Agregar callbacks `@PrePersist` / `@PreUpdate` con `Instant.now()`
    - Agregar `equals`/`hashCode` basado en id
    - Mantener `Instant` para timestamps y `name` length=150
    - _Requisitos: 9.1_

  - [x] 1.2 Actualizar imports en el módulo auth para usar la entidad canónica
    - Modificar `AuthService.java` → usar `py.com.servipy.user.domain.User` y `Role`
    - Modificar `JwtService.java` → usar `py.com.servipy.user.domain.User`
    - Modificar `UserRepository.java` → usar `py.com.servipy.user.domain.User`
    - Modificar `JwtAuthenticationFilter.java` → usar `py.com.servipy.user.domain.User`
    - Modificar `UserResponse.java` → usar `py.com.servipy.user.domain.Role`
    - Modificar `AuthController.java` → usar `py.com.servipy.user.domain.User`
    - _Requisitos: 9.1, 9.2, 9.3_

  - [x] 1.3 Eliminar clases duplicadas del paquete auth.domain
    - Eliminar `py.com.servipy.auth.domain.User.java`
    - Eliminar `py.com.servipy.auth.domain.Role.java`
    - Verificar que el proyecto compila sin errores
    - _Requisitos: 9.2_

  - [x] 1.4 Agregar configuración CORS en SecurityConfig
    - Agregar property `app.cors.allowed-origin` configurable por variable de entorno
    - Crear bean `CorsConfigurationSource` con métodos GET, POST, PUT, DELETE, OPTIONS y headers Authorization, Content-Type
    - Agregar `.cors(Customizer.withDefaults())` al SecurityFilterChain
    - _Requisitos: 8.1, 8.2, 8.3, 8.4_

  - [ ]* 1.5 Escribir tests unitarios para consolidación y CORS
    - Test que verifica que `User` se persiste correctamente con timestamps automáticos
    - Test que verifica la configuración CORS con requests OPTIONS (preflight)
    - Test de integración del flujo registro → login con la entidad canónica
    - _Requisitos: 9.1, 8.4_

- [ ] 2. Checkpoint - Verificar compilación backend
  - Asegurar que todos los tests pasan y el proyecto compila correctamente tras la consolidación. Consultar al usuario si surgen dudas.

- [ ] 3. Modelos e interfaces TypeScript para autenticación
  - [x] 3.1 Crear interfaces de modelos de autenticación en el frontend
    - Crear archivo `src/app/shared/models/auth.model.ts`
    - Definir interfaces: `LoginRequest`, `RegisterRequest`, `AuthResponse`, `UserResponse`
    - Exportar desde el barrel del módulo shared si existe
    - _Requisitos: 11.1, 11.2, 14.1, 15.1_

- [ ] 4. Servicio de autenticación frontend
  - [x] 4.1 Implementar `AuthService` en `src/app/core/auth/auth.service.ts`
    - Crear servicio con `providedIn: 'root'`
    - Implementar signal `currentUser` de tipo `WritableSignal<UserResponse | null>`
    - Implementar `login(email, password): Observable<AuthResponse>` que invoque POST `/api/v1/auth/login` y almacene token + actualice signal
    - Implementar `register(name, email, password, roleType): Observable<AuthResponse>` que invoque POST al endpoint correspondiente según roleType
    - Implementar `logout()`: eliminar token de localStorage, limpiar signal, redirigir a `/login`
    - Implementar `getToken(): string | null` desde localStorage
    - Implementar `isAuthenticated(): boolean` basado en existencia de token
    - Implementar `getUserRole(): string | null` decodificando el JWT
    - Implementar `restoreSession()` para recuperar sesión al iniciar la app
    - _Requisitos: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7, 10.1, 10.2, 10.3, 16.1, 16.2, 16.3_

  - [ ]* 4.2 Escribir tests unitarios para AuthService
    - Test de login exitoso: verifica que almacena token y actualiza currentUser
    - Test de register exitoso: verifica endpoint correcto según roleType
    - Test de logout: verifica eliminación de token, limpieza de signal, redirección
    - Test de getToken: retorna token o null
    - Test de isAuthenticated: retorna true/false según token
    - Test de getUserRole: decodifica role del JWT
    - Test de restoreSession: restaura sesión si hay token válido
    - _Requisitos: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7_

  - [ ]* 4.3 Escribir test de propiedad para token storage round-trip
    - **Property 13: Token storage round-trip**
    - Generar strings de token aleatorios, almacenar via AuthService y verificar que getToken() retorna el mismo string. Tras logout, getToken() retorna null.
    - **Valida: Requisitos 16.1, 16.2**

- [ ] 5. Interceptor JWT frontend
  - [x] 5.1 Implementar `jwtInterceptor` en `src/app/core/http/jwt.interceptor.ts`
    - Crear interceptor funcional (`HttpInterceptorFn`)
    - Si hay token en localStorage, clonar request con header `Authorization: Bearer {token}`
    - Si la respuesta es HTTP 401, ejecutar logout y redirigir a `/login`
    - Si no hay token, enviar request sin modificar
    - _Requisitos: 12.1, 12.2, 12.3_

  - [x] 5.2 Registrar el interceptor en la configuración de la app
    - Agregar `jwtInterceptor` a `provideHttpClient(withInterceptors([...]))` en `app.config.ts`
    - Asegurar que se agrega junto al `errorInterceptor` existente
    - _Requisitos: 12.1_

  - [ ]* 5.3 Escribir tests unitarios para jwtInterceptor
    - Test que verifica que adjunta token cuando existe
    - Test que verifica que no modifica request sin token
    - Test que verifica que ejecuta logout en HTTP 401
    - _Requisitos: 12.1, 12.2, 12.3_

  - [ ]* 5.4 Escribir test de propiedad para el interceptor
    - **Property 11: Interceptor adjunta token cuando existe**
    - Generar requests aleatorias con/sin token en localStorage, verificar que el interceptor adjunta o no el header correctamente.
    - **Valida: Requisitos 12.1, 12.3**

- [ ] 6. Guards de navegación
  - [x] 6.1 Implementar `authGuard` en `src/app/core/guards/auth.guard.ts`
    - Crear guard funcional (`CanActivateFn`)
    - Si `isAuthenticated()` es true → retornar true
    - Si no → redirigir a `/login`, retornar false
    - _Requisitos: 13.1, 13.2_

  - [x] 6.2 Implementar `roleGuard` en `src/app/core/guards/role.guard.ts`
    - Crear guard funcional (`CanActivateFn`)
    - Leer roles permitidos de `route.data['roles']`
    - Si `getUserRole()` está en la lista → retornar true
    - Si no → redirigir a `/`, retornar false
    - _Requisitos: 13.3, 13.4, 13.5_

  - [ ]* 6.3 Escribir tests unitarios para guards
    - Test authGuard: permite navegación si autenticado, redirige si no
    - Test roleGuard: permite si rol coincide, redirige si no
    - Test roleGuard: lee configuración de roles desde route data
    - _Requisitos: 13.1, 13.2, 13.3, 13.4, 13.5_

  - [ ]* 6.4 Escribir test de propiedad para guards
    - **Property 12: Guards aplican reglas de autenticación y rol**
    - Generar estados de auth (autenticado/no, con diferentes roles) y configuraciones de ruta (lista de roles permitidos). Verificar que authGuard retorna true si y solo si autenticado, y roleGuard retorna true si y solo si el rol está en la lista.
    - **Valida: Requisitos 13.1, 13.2, 13.3, 13.4**

- [ ] 7. Checkpoint - Verificar servicios y guards frontend
  - Asegurar que todos los tests pasan. Consultar al usuario si surgen dudas.

- [ ] 8. Página de Login
  - [x] 8.1 Crear componente `LoginComponent` en `src/app/features/authentication/login/`
    - Componente standalone con formulario reactivo (ReactiveFormsModule)
    - Campos: email (required, email format) y password (required)
    - Botón submit deshabilitado durante procesamiento con indicador de carga
    - Mensajes de error con `role="alert"` para accesibilidad
    - Labels asociados a inputs, navegable por teclado
    - _Requisitos: 14.1, 14.7, 14.9_

  - [x] 8.2 Implementar lógica de submit y manejo de errores en LoginComponent
    - Al enviar, invocar `AuthService.login(email, password)`
    - Login exitoso: redirigir según rol (CLIENT → `/client`, PROFESSIONAL → `/professional`, ADMIN → `/admin`)
    - Login fallido: mostrar mensaje de error genérico visible
    - Enlace de navegación a página de registro
    - _Requisitos: 14.2, 14.3, 14.4, 14.5, 14.6, 14.8_

  - [ ]* 8.3 Escribir tests de componente para LoginComponent
    - Test de renderizado: formulario con email, password y botón submit
    - Test de validación: botón deshabilitado con datos inválidos
    - Test de submit exitoso: redirige según rol
    - Test de submit fallido: muestra error
    - Test de accesibilidad: labels asociados, alerts presentes
    - _Requisitos: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7, 14.9_

- [ ] 9. Página de Registro
  - [x] 9.1 Crear componente `RegisterComponent` en `src/app/features/authentication/register/`
    - Componente standalone con formulario reactivo
    - Campos: name (required, min 2 chars), email (required, email format), password (required, min 8 chars), selector tipo de cuenta (CLIENT/PROFESSIONAL)
    - Botón submit deshabilitado durante procesamiento con indicador de carga
    - Validación frontend de password mínimo 8 caracteres
    - Mensajes de error con `role="alert"`, labels asociados, navegable por teclado
    - _Requisitos: 15.1, 15.6, 15.7, 15.9_

  - [x] 9.2 Implementar lógica de submit y manejo de errores en RegisterComponent
    - Al enviar, invocar `AuthService.register(name, email, password, roleType)`
    - Registro exitoso: redirigir según rol (mismas reglas que login)
    - Error email duplicado: mostrar mensaje "el email ya está registrado"
    - Errores de validación: mostrar mensajes junto a cada campo
    - Enlace de navegación a página de login
    - _Requisitos: 15.2, 15.3, 15.4, 15.5, 15.8_

  - [ ]* 9.3 Escribir tests de componente para RegisterComponent
    - Test de renderizado: formulario con name, email, password, selector y botón
    - Test de validación frontend: password mínimo 8 chars, name mínimo 2 chars
    - Test de submit exitoso: redirige según rol
    - Test de submit fallido (duplicado): muestra mensaje de email duplicado
    - Test de submit fallido (validación): muestra errores por campo
    - _Requisitos: 15.1, 15.2, 15.3, 15.4, 15.5, 15.7_

- [ ] 10. Rutas y wiring del frontend
  - [x] 10.1 Actualizar rutas en `app.routes.ts` con guards
    - Eliminar/reemplazar el placeholder de login por la nueva ruta
    - Agregar ruta `/login` apuntando a `LoginComponent`
    - Agregar ruta `/register` apuntando a `RegisterComponent`
    - Proteger rutas de `/client/**` con `authGuard` y `roleGuard` (roles: ['CLIENT'])
    - Proteger rutas de `/professional/**` con `authGuard` y `roleGuard` (roles: ['PROFESSIONAL'])
    - Proteger rutas de `/admin/**` con `authGuard` y `roleGuard` (roles: ['ADMIN'])
    - _Requisitos: 13.1, 13.2, 13.3, 13.4, 14.3, 14.4, 14.5_

  - [x] 10.2 Integrar restauración de sesión en inicialización de la app
    - Llamar `AuthService.restoreSession()` en el arranque de la aplicación (APP_INITIALIZER o constructor del AppComponent)
    - _Requisitos: 16.3_

- [ ] 11. Checkpoint - Verificar integración frontend completa
  - Asegurar que todos los tests pasan. Verificar que las rutas protegidas redirigen correctamente. Consultar al usuario si surgen dudas.

- [ ] 12. Tests de propiedad backend (jqwik)
  - [ ]* 12.1 Escribir test de propiedad: Registro produce usuario válido con rol correcto
    - **Property 1: Registro produce usuario válido con rol correcto**
    - Generar tuplas válidas (name, email, password) y roles (CLIENT, PROFESSIONAL). Verificar que el registro crea usuario con rol correcto, active=true, passwordHash válido y retorna AuthResponse con JWT válido.
    - **Valida: Requisitos 1.1, 2.1**

  - [ ]* 12.2 Escribir test de propiedad: Detección de email duplicado es case-insensitive
    - **Property 2: Detección de email duplicado es case-insensitive**
    - Generar emails registrados exitosamente, crear variantes de capitalización, verificar rechazo con DUPLICATE_EMAIL.
    - **Valida: Requisitos 1.2**

  - [ ]* 12.3 Escribir test de propiedad: Inputs inválidos de registro son rechazados
    - **Property 3: Inputs inválidos de registro son rechazados**
    - Generar inputs que violen restricciones (name fuera de 2-100, email inválido, password fuera de 8-72). Verificar HTTP 400.
    - **Valida: Requisitos 1.3, 1.4, 1.5, 2.2**

  - [ ]* 12.4 Escribir test de propiedad: Normalización de datos en registro
    - **Property 4: Normalización de datos en registro**
    - Generar names con whitespace y emails con mayúsculas. Verificar que el email persistido es lowercase y el name está trimmed.
    - **Valida: Requisitos 1.6**

  - [ ]* 12.5 Escribir test de propiedad: Round-trip registro-login
    - **Property 5: Round-trip registro-login**
    - Registrar usuario, hacer login con mismas credenciales (email con case variante). Verificar AuthResponse válido.
    - **Valida: Requisitos 3.1, 3.6**

  - [ ]* 12.6 Escribir test de propiedad: Credenciales incorrectas producen error genérico
    - **Property 6: Credenciales incorrectas producen error genérico**
    - Generar emails inexistentes o passwords incorrectos. Verificar HTTP 401 con mensaje genérico.
    - **Valida: Requisitos 3.2**

  - [ ]* 12.7 Escribir test de propiedad: JWT contiene claims correctos
    - **Property 7: JWT contiene claims correctos**
    - Generar usuarios con diferentes datos. Verificar claims sub=userId, email, role y expiración 15-30 min.
    - **Valida: Requisitos 4.1**

  - [ ]* 12.8 Escribir test de propiedad: JWT rechaza tokens inválidos
    - **Property 8: JWT rechaza tokens inválidos o alterados**
    - Generar strings aleatorios y tokens con firma alterada. Verificar que isTokenValid() retorna false.
    - **Valida: Requisitos 4.2, 4.3**

- [ ] 13. Checkpoint final - Asegurar que todos los tests pasan
  - Ejecutar suite completa de tests backend y frontend. Consultar al usuario si surgen dudas.

## Notas

- Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido
- Cada tarea referencia requisitos específicos para trazabilidad
- Los checkpoints aseguran validación incremental
- Los tests de propiedad validan propiedades de corrección universales definidas en el diseño
- Los tests unitarios validan ejemplos específicos y casos borde
- Backend usa Java (Spring Boot 3.x) con jqwik para property tests
- Frontend usa TypeScript (Angular 19) con fast-check para property tests

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "3.1"] },
    { "id": 1, "tasks": ["1.2", "4.1"] },
    { "id": 2, "tasks": ["1.3", "1.4", "4.2", "4.3", "5.1"] },
    { "id": 3, "tasks": ["1.5", "5.2", "5.3", "5.4", "6.1", "6.2"] },
    { "id": 4, "tasks": ["6.3", "6.4", "8.1"] },
    { "id": 5, "tasks": ["8.2", "9.1"] },
    { "id": 6, "tasks": ["8.3", "9.2"] },
    { "id": 7, "tasks": ["9.3", "10.1", "10.2"] },
    { "id": 8, "tasks": ["12.1", "12.2", "12.3", "12.4"] },
    { "id": 9, "tasks": ["12.5", "12.6", "12.7", "12.8"] }
  ]
}
```
