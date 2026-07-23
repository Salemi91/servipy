# Plan de Implementación: Fundación del Proyecto y Autenticación

## Resumen

Plan de implementación para la capa fundacional del monorepo ServiPy y el sistema de autenticación JWT. Las tareas están organizadas para permitir trabajo paralelo entre frontend y backend, con dependencias claras entre las fases.

## Tareas

- [ ] 1. chore: Inicializar proyecto backend con Spring Boot
  - [ ] 1.1 Crear proyecto Spring Boot con Maven wrapper
    - Generar estructura `backend/` con Spring Boot 3.x, Java 21
    - Incluir dependencias: spring-boot-starter-web, spring-boot-starter-security, spring-boot-starter-data-jpa, spring-boot-starter-validation, mysql-connector-j, jjwt (api, impl, jackson), lombok, spring-boot-starter-test, jqwik
    - Configurar `pom.xml` con todas las dependencias
    - Crear paquete base `com.servipy.api`
    - Verificar que `./mvnw compile` pasa sin errores
    - _Requisitos: 1.1, 1.2_

  - [ ] 1.2 Crear archivo `application.yml` y configuración de datasource
    - Configurar datasource con variables de entorno `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`
    - Configurar JPA con hibernate ddl-auto: validate, dialecto MySQL
    - Configurar Jackson para fechas ISO 8601 (write-dates-as-timestamps: false)
    - Agregar propiedades custom: `app.jwt.secret`, `app.jwt.expiration-minutes`, `app.admin.*`
    - _Requisitos: 2.1, 2.2, 2.3, 8.6_

  - [ ] 1.3 Implementar `EnvValidationConfig` para validación de variables de entorno
    - Crear clase que implemente `ApplicationRunner`
    - Validar presencia y formato de: DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET, JWT_EXPIRATION_MINUTES, ADMIN_NAME, ADMIN_EMAIL, ADMIN_PASSWORD
    - Lanzar `IllegalStateException` con nombre de variable si falta o está vacía
    - Validar que JWT_EXPIRATION_MINUTES sea numérico
    - Validar formato URL para DB_URL
    - _Requisitos: 2.4, 2.6, 2.7_

  - [ ] 1.4 Crear archivo `.env.example` en la raíz del monorepo
    - Listar todas las variables requeridas con valores de ejemplo no funcionales
    - Incluir comentario descriptivo por cada variable
    - _Requisitos: 2.5_

- [ ] 2. chore: Inicializar proyecto frontend con Angular y Tailwind
  - [ ] 2.1 Crear proyecto Angular en `frontend/`
    - Generar proyecto Angular 17+ con routing habilitado y estilo CSS
    - Configurar Tailwind CSS (instalar, crear tailwind.config.js, agregar directivas en styles.css)
    - Crear estructura de directorios: `core/`, `features/`, `shared/`
    - Verificar que `ng build` compila sin errores
    - Verificar que clases utilitarias de Tailwind se resuelven en el CSS de salida
    - _Requisitos: 1.1_

  - [ ] 2.2 Crear modelos TypeScript y estructura base del core
    - Crear `core/models/user.model.ts` con interfaces User, Role
    - Crear `core/models/auth-response.model.ts` con interfaces AuthResponse, ErrorResponse, FieldError
    - Crear estructura de directorios: `core/guards/`, `core/interceptors/`, `core/services/`
    - Crear directorios de features: `features/auth/`, `features/client/`, `features/professional/`, `features/admin/`
    - _Requisitos: 10.6_

- [ ] 3. chore: Crear migración SQL y estructura de base de datos
  - [ ] 3.1 Crear script `database/V1__create_users_table.sql`
    - Definir tabla `users` con columnas: id, name, email, password_hash, role (ENUM), active, created_at, updated_at
    - Incluir índices en email y role
    - Usar ENGINE=InnoDB, charset utf8mb4
    - _Requisitos: 1.5_

- [ ] 4. feat: Implementar entidad User, Role enum y repositorio
  - [ ] 4.1 Crear enum `Role.java` y entidad `User.java`
    - Enum Role con valores CLIENT, PROFESSIONAL, ADMIN
    - Entidad User con anotaciones JPA (@Entity, @Table, @Id, @GeneratedValue, @Enumerated, etc.)
    - Implementar @PrePersist y @PreUpdate para timestamps
    - Configurar unique constraint en email
    - _Requisitos: 9.1_

  - [ ] 4.2 Crear `UserRepository.java`
    - Extender JpaRepository<User, Long>
    - Método `Optional<User> findByEmailIgnoreCase(String email)`
    - Método `boolean existsByEmailIgnoreCase(String email)`
    - _Requisitos: 4.3, 5.3_

- [ ] 5. feat: Implementar DTOs de request y response
  - [ ] 5.1 Crear DTOs del backend
    - `RegisterRequest` record con validaciones: @NotBlank, @Size, @Email
    - `LoginRequest` record con validaciones
    - `AuthResponse` record con accessToken, tokenType, user
    - `UserResponse` record con id, name, email, role
    - `ErrorResponse` record con timestamp, status, code, message, errors[]
    - _Requisitos: 8.4, 12.1_

- [ ] 6. feat: Implementar JwtService para generación y validación de tokens
  - [ ] 6.1 Crear `JwtService.java`
    - Método `generateToken(User user)`: crear JWT con claims sub (userId), email, role, exp
    - Método `isTokenValid(String token)`: verificar firma y expiración
    - Método `extractUserId(String token)`: extraer claim sub
    - Método `extractEmail(String token)`: extraer email
    - Método `extractRole(String token)`: extraer role
    - Usar clave secreta desde variable de entorno vía @Value
    - Calcular expiración desde JWT_EXPIRATION_MINUTES
    - _Requisitos: 6.6, 8.1, 8.5_

  - [ ]* 6.2 Escribir property test para JWT claims (Propiedad 7)
    - **Propiedad 7: Login exitoso genera JWT con claims correctos**
    - Generar usuarios aleatorios, verificar que JWT contiene sub, email, role, exp correctos
    - Configurar mínimo 100 iteraciones con jqwik
    - **Valida: Requisitos 6.1, 6.6, 8.5**

- [ ] 7. feat: Implementar SecurityConfig y JwtAuthenticationFilter
  - [ ] 7.1 Crear `SecurityConfig.java`
    - Deshabilitar CSRF (API stateless)
    - Configurar sesión como STATELESS
    - Definir endpoints públicos: /api/v1/health, /api/v1/auth/login, /api/v1/auth/register/**
    - Definir endpoints admin: /api/v1/admin/** → requiere ADMIN
    - Resto: authenticated
    - Agregar JwtAuthenticationFilter antes de UsernamePasswordAuthenticationFilter
    - _Requisitos: 8.7, 9.4, 9.5, 9.6_

  - [ ] 7.2 Crear `JwtAuthenticationFilter.java`
    - Extender OncePerRequestFilter
    - Extraer token del header Authorization (formato Bearer)
    - Si no hay token → continuar sin autenticar
    - Validar firma y expiración
    - Verificar que usuario existe y está activo en BD
    - Establecer Authentication en SecurityContextHolder
    - Manejar token expirado con respuesta TOKEN_EXPIRED
    - _Requisitos: 8.1, 8.2, 8.3, 7.2, 7.3, 7.4_

  - [ ] 7.3 Crear `CorsConfig.java`
    - Permitir origin http://localhost:4200
    - Permitir métodos GET, POST, PUT, PATCH, DELETE, OPTIONS
    - Permitir todos los headers
    - _Requisitos: 1.1_

  - [ ]* 7.4 Escribir property test para tokens inválidos (Propiedad 10)
    - **Propiedad 10: Endpoints protegidos rechazan tokens inválidos**
    - Generar tokens malformados, con firma de otra clave, sin token
    - Verificar respuesta 401 UNAUTHORIZED
    - **Valida: Requisitos 7.2, 7.4, 8.1, 8.2**

- [ ] 8. feat: Implementar GlobalExceptionHandler y excepciones custom
  - [ ] 8.1 Crear excepciones custom y GlobalExceptionHandler
    - Crear `DuplicateEmailException`, `AccountInactiveException`
    - Implementar `GlobalExceptionHandler` con @RestControllerAdvice
    - Manejar: MethodArgumentNotValidException → 400 VALIDATION_ERROR
    - Manejar: HttpMessageNotReadableException → 400 MALFORMED_REQUEST
    - Manejar: DuplicateEmailException → 409 DUPLICATE_EMAIL
    - Manejar: AccountInactiveException → 403 ACCOUNT_INACTIVE
    - Manejar: HttpRequestMethodNotSupportedException → 405 METHOD_NOT_ALLOWED
    - Manejar: AccessDeniedException → 403 FORBIDDEN
    - Manejar: Exception general → 500 INTERNAL_ERROR (sin info sensible)
    - Formatear timestamp en ISO 8601
    - Mensajes de error en español
    - _Requisitos: 12.1, 12.2, 12.3, 12.4, 12.5_

  - [ ]* 8.2 Escribir property test para estructura de error uniforme (Propiedad 18)
    - **Propiedad 18: Todas las respuestas de error siguen la estructura uniforme**
    - Enviar requests inválidos variados, verificar campos timestamp, status, code, message, errors[]
    - **Valida: Requisito 12.1**

  - [ ]* 8.3 Escribir property test para body no-JSON (Propiedad 21)
    - **Propiedad 21: Cuerpo no-JSON retorna MALFORMED_REQUEST**
    - Generar strings aleatorios no-JSON como body
    - Verificar respuesta 400 con code MALFORMED_REQUEST
    - **Valida: Requisito 12.4**

- [ ] 9. feat: Implementar HealthController
  - [ ] 9.1 Crear `HealthController.java`
    - Endpoint GET /api/v1/health que retorna `{"status": "UP"}` con HTTP 200
    - Content-Type: application/json
    - Accesible sin autenticación (ya configurado en SecurityConfig)
    - _Requisitos: 3.1, 3.2, 3.3_

- [ ] 10. feat: Implementar AuthService con registro y login
  - [ ] 10.1 Crear `AuthService.java` con lógica de registro
    - Método `registerClient(RegisterRequest)`: verificar email único, hash bcrypt, crear User con role=CLIENT, generar JWT, retornar AuthResponse
    - Método `registerProfessional(RegisterRequest)`: igual pero con role=PROFESSIONAL
    - Comparación de email case-insensitive
    - Trim de name antes de validar longitud
    - _Requisitos: 4.1, 4.2, 4.3, 4.7, 5.1, 5.2, 5.3, 5.7, 9.2_

  - [ ] 10.2 Implementar lógica de login en `AuthService.java`
    - Método `login(LoginRequest)`: buscar usuario por email, verificar password con BCrypt.matches(), verificar active=true, generar JWT, retornar AuthResponse
    - Lanzar excepción genérica para email no encontrado o password incorrecto (sin revelar cuál)
    - Lanzar AccountInactiveException si active=false
    - _Requisitos: 6.1, 6.2, 6.3, 6.4, 6.5_

  - [ ] 10.3 Implementar método `getCurrentUser` en AuthService
    - Recibir userId, buscar en BD, retornar UserResponse
    - Si usuario no existe o está inactivo → 401
    - _Requisitos: 7.1_

  - [ ]* 10.4 Escribir property tests para registro (Propiedades 1, 3, 4, 5, 6)
    - **Propiedad 1: Registro exitoso asigna rol según endpoint**
    - **Propiedad 3: Unicidad de email (detección de duplicados)**
    - **Propiedad 4: Validación de email rechaza formatos inválidos**
    - **Propiedad 5: Validación de longitud de password**
    - **Propiedad 6: Validación de longitud de name (trimmed)**
    - Generar datos aleatorios válidos e inválidos con jqwik
    - **Valida: Requisitos 4.1, 4.3, 4.5, 4.6, 4.7, 5.1, 5.3, 5.5, 5.6, 5.7, 9.2**

  - [ ]* 10.5 Escribir property test para credenciales incorrectas (Propiedad 8)
    - **Propiedad 8: Credenciales incorrectas retornan 401 sin revelar causa**
    - Generar emails no registrados y passwords incorrectos aleatorios
    - Verificar siempre 401 INVALID_CREDENTIALS
    - **Valida: Requisitos 6.3, 6.4**

- [ ] 11. feat: Implementar AuthController
  - [ ] 11.1 Crear `AuthController.java` con endpoints de autenticación
    - POST /api/v1/auth/register/client → delegar a AuthService.registerClient, retornar 201
    - POST /api/v1/auth/register/professional → delegar a AuthService.registerProfessional, retornar 201
    - POST /api/v1/auth/login → delegar a AuthService.login, retornar 200
    - GET /api/v1/auth/me → extraer userId de SecurityContext, delegar a AuthService.getCurrentUser, retornar 200
    - Usar @Valid en @RequestBody para activar validación
    - _Requisitos: 4.1, 5.1, 6.1, 7.1_

- [ ] 12. Checkpoint — Verificar backend core
  - Ensure all tests pass, ask the user if questions arise.
  - Verificar que `./mvnw compile` pasa
  - Verificar que los endpoints públicos responden correctamente

- [ ] 13. feat: Implementar AdminSeeder
  - [ ] 13.1 Crear `AdminSeeder.java`
    - Implementar ApplicationRunner
    - Leer ADMIN_NAME, ADMIN_EMAIL, ADMIN_PASSWORD de variables de entorno
    - Buscar usuario por email (case-insensitive)
    - Si no existe → crear usuario con role=ADMIN, active=true, password hasheada con bcrypt
    - Si ya existe → log info y continuar sin error
    - _Requisitos: 11.1, 11.2, 11.3, 11.4, 11.5_

  - [ ]* 13.2 Escribir property test para idempotencia del seed (Propiedad 16)
    - **Propiedad 16: Idempotencia del seed de administrador**
    - Ejecutar seed múltiples veces, verificar que no crea duplicados
    - **Valida: Requisito 11.4**

- [ ] 14. feat: Implementar AuthService en el frontend
  - [ ] 14.1 Crear `AuthService` en Angular
    - Implementar métodos: login(), registerClient(), registerProfessional(), getCurrentUser(), logout(), getToken(), isAuthenticated(), getUserRole()
    - Almacenar token en localStorage bajo clave `accessToken`
    - Usar signals para estado del usuario actual
    - Conectar con endpoints del backend via HttpClient
    - _Requisitos: 10.3, 10.4_

  - [ ] 14.2 Crear `AuthInterceptor` funcional
    - Implementar HttpInterceptorFn
    - Si hay token en localStorage → agregar header Authorization: Bearer <token>
    - Si respuesta es 401 → llamar logout(), redirigir a /login
    - Registrar interceptor en app.config.ts
    - _Requisitos: 10.4, 10.5_

- [ ] 15. feat: Implementar guards y rutas del frontend
  - [ ] 15.1 Crear AuthGuard y RoleGuard
    - `authGuard` (CanActivateFn): verificar isAuthenticated(), si no → redirigir a /login
    - `roleGuard` (CanActivateFn): leer rol requerido de route.data['role'], comparar con rol del usuario, si no coincide → redirigir a página principal según rol (CLIENT→/client, PROFESSIONAL→/professional, ADMIN→/admin)
    - _Requisitos: 10.1, 10.2_

  - [ ] 15.2 Configurar rutas con guards en `app.routes.ts`
    - Rutas públicas: /login, /register
    - Rutas de cliente: /client/** (authGuard + roleGuard con role=CLIENT)
    - Rutas de profesional: /professional/** (authGuard + roleGuard con role=PROFESSIONAL)
    - Rutas de admin: /admin/** (authGuard + roleGuard con role=ADMIN)
    - Ruta por defecto redirige a /login
    - Crear componentes placeholder para cada sección
    - _Requisitos: 10.6_

- [ ] 16. feat: Implementar páginas de Login y Registro
  - [ ] 16.1 Crear componentes de Login y Register
    - LoginComponent: formulario con email y password, llamar a AuthService.login(), redirigir según rol tras éxito
    - RegisterComponent: formulario con name, email, password, selector de tipo (cliente/profesional), llamar al endpoint correspondiente
    - Mostrar errores de validación del backend usando la estructura ErrorResponse
    - Usar clases de Tailwind CSS para estilos
    - _Requisitos: 4.1, 5.1, 6.1_

- [ ] 17. Checkpoint — Verificar integración frontend-backend
  - Ensure all tests pass, ask the user if questions arise.
  - Verificar que el flujo completo de registro → login → acceso a ruta protegida funciona
  - Verificar que guards redirigen correctamente

- [ ] 18. test: Implementar pruebas de integración de autenticación
  - [ ] 18.1 Crear `AuthControllerIntegrationTest.java`
    - Configurar @SpringBootTest con MockMvc y base de datos de test (H2 o Testcontainers MySQL)
    - Test: registro exitoso de cliente → 201 con accessToken, tokenType, user.role=CLIENT
    - Test: registro con email duplicado → 409 DUPLICATE_EMAIL
    - Test: login exitoso → 200 con accessToken, tokenType, user
    - Test: login con password incorrecto → 401 INVALID_CREDENTIALS
    - Test: login con email no registrado → 401 INVALID_CREDENTIALS
    - Test: GET /auth/me sin token → 401
    - Test: GET /auth/me con token expirado → 401
    - Test: CLIENT accediendo a /admin/** → 403
    - _Requisitos: 13.1, 13.2, 13.3, 13.4, 13.5, 13.6, 13.7_

  - [ ]* 18.2 Escribir property test para bcrypt storage (Propiedad 2)
    - **Propiedad 2: Contraseña siempre almacenada como hash bcrypt**
    - Registrar usuarios aleatorios, verificar que passwordHash en BD empieza con $2a$ o $2b$
    - Verificar que response no contiene el hash
    - **Valida: Requisitos 4.2, 5.2, 11.2**

- [ ] 19. chore: Actualizar README.md y documentación
  - [ ] 19.1 Actualizar README.md de la raíz
    - Describir estructura de directorios del monorepo
    - Listar tecnologías utilizadas (Angular, Spring Boot, MySQL, JWT)
    - Agregar enlaces a documentación en docs/
    - Incluir instrucciones de setup local (clonar, copiar .env, levantar BD, ejecutar backend y frontend)
    - _Requisitos: 1.6_

- [ ] 20. Checkpoint final — Verificar criterios de aceptación completos
  - Ensure all tests pass, ask the user if questions arise.
  - Verificar que `./mvnw compile` y `ng build` pasan sin errores
  - Verificar que todas las pruebas de integración del Requisito 13 pasan
  - Verificar que el .env.example contiene todas las variables documentadas

## Notas

- Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido
- Cada tarea referencia los requisitos específicos que cumple para trazabilidad
- Los checkpoints aseguran validación incremental
- Las property tests validan propiedades universales de correctitud definidas en el diseño
- Las pruebas de integración validan los flujos específicos del Requisito 13
- Las tareas de backend (1, 3, 4-13) y frontend (2, 14-16) pueden desarrollarse en paralelo por distintos miembros del equipo
- La tarea 18 (pruebas de integración) requiere que el backend esté completo
- Seguir la estrategia de ramas: crear rama `feature/authentication` para todo el trabajo

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "2.1", "3.1"] },
    { "id": 1, "tasks": ["1.2", "1.3", "1.4", "2.2"] },
    { "id": 2, "tasks": ["4.1"] },
    { "id": 3, "tasks": ["4.2", "5.1"] },
    { "id": 4, "tasks": ["6.1", "8.1", "9.1"] },
    { "id": 5, "tasks": ["6.2", "7.1", "7.2", "7.3"] },
    { "id": 6, "tasks": ["7.4", "8.2", "8.3", "10.1"] },
    { "id": 7, "tasks": ["10.2", "10.3"] },
    { "id": 8, "tasks": ["10.4", "10.5", "11.1"] },
    { "id": 9, "tasks": ["13.1", "14.1"] },
    { "id": 10, "tasks": ["13.2", "14.2", "15.1"] },
    { "id": 11, "tasks": ["15.2", "16.1"] },
    { "id": 12, "tasks": ["18.1", "18.2", "19.1"] }
  ]
}
```
