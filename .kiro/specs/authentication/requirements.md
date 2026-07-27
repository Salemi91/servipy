# Documento de Requisitos — Autenticación y Autorización

## Introducción

Este documento define los requisitos para la infraestructura de autenticación y autorización de ServiPy. Cubre el registro de usuarios, inicio de sesión, gestión de tokens JWT, autorización basada en roles, y los componentes frontend necesarios para la interacción del usuario con el sistema de autenticación. No incluye funcionalidad de negocio específica de perfiles profesionales, catálogo de servicios, solicitudes, ni panel de administración.

## Glosario

- **Sistema_Auth**: Módulo backend de autenticación ubicado en `py.com.servipy.auth`, responsable de registro, login y gestión de tokens.
- **JWT_Service**: Servicio encargado de generar, validar y extraer claims de tokens JWT.
- **Security_Filter**: Filtro HTTP (`JwtAuthenticationFilter`) que intercepta cada request, valida el token y establece el contexto de seguridad.
- **Auth_Controller**: Controlador REST que expone los endpoints de autenticación bajo `/api/v1/auth`.
- **Frontend_AuthService**: Servicio Angular singleton encargado de gestionar el estado de autenticación en el cliente.
- **JWT_Interceptor**: Interceptor HTTP de Angular que adjunta el token Bearer a las peticiones salientes.
- **Auth_Guard**: Guard de Angular que protege rutas que requieren autenticación.
- **Role_Guard**: Guard de Angular que protege rutas que requieren un rol específico.
- **User_Entity**: Entidad JPA canónica ubicada en `py.com.servipy.user.domain.User` con campos: id, name, email, passwordHash, role, active, createdAt, updatedAt.
- **Role**: Enum con valores CLIENT, PROFESSIONAL, ADMIN que define el nivel de acceso del usuario.
- **Access_Token**: Token JWT de vida corta (15-30 minutos) usado para autenticar peticiones.
- **BCrypt**: Algoritmo de hashing para contraseñas.

## Requisitos

### Requisito 1: Registro de Cliente

**User Story:** Como visitante, quiero registrarme como cliente proporcionando mi nombre, email y contraseña, para poder acceder a las funcionalidades de cliente de ServiPy.

#### Criterios de Aceptación

1. WHEN un visitante envía una petición POST a `/api/v1/auth/register/client` con name, email y password válidos, THE Sistema_Auth SHALL crear un nuevo User_Entity con role CLIENT, active=true, passwordHash generado con BCrypt, y retornar un AuthResponse con Access_Token y datos del usuario con HTTP 201.
2. WHEN un visitante envía una petición de registro con un email que ya existe en el sistema (comparación case-insensitive), THE Sistema_Auth SHALL retornar un error HTTP 409 con código DUPLICATE_EMAIL.
3. WHEN un visitante envía una petición de registro con name vacío o menor a 2 caracteres o mayor a 100 caracteres, THE Sistema_Auth SHALL retornar un error HTTP 400 con los detalles de validación.
4. WHEN un visitante envía una petición de registro con email en formato inválido o mayor a 255 caracteres, THE Sistema_Auth SHALL retornar un error HTTP 400 con los detalles de validación.
5. WHEN un visitante envía una petición de registro con password menor a 8 caracteres o mayor a 72 caracteres, THE Sistema_Auth SHALL retornar un error HTTP 400 con los detalles de validación.
6. WHEN un registro es exitoso, THE Sistema_Auth SHALL normalizar el email a minúsculas y eliminar espacios en blanco al inicio y final del name antes de persistir.

### Requisito 2: Registro de Profesional

**User Story:** Como visitante, quiero registrarme como profesional proporcionando mi nombre, email y contraseña, para poder ofrecer mis servicios en ServiPy.

#### Criterios de Aceptación

1. WHEN un visitante envía una petición POST a `/api/v1/auth/register/professional` con name, email y password válidos, THE Sistema_Auth SHALL crear un nuevo User_Entity con role PROFESSIONAL, active=true, passwordHash generado con BCrypt, y retornar un AuthResponse con Access_Token y datos del usuario con HTTP 201.
2. WHEN un visitante envía una petición de registro de profesional con datos inválidos, THE Sistema_Auth SHALL aplicar las mismas reglas de validación que el registro de cliente (Requisito 1, criterios 2-6).

### Requisito 3: Inicio de Sesión

**User Story:** Como usuario registrado, quiero iniciar sesión con mi email y contraseña, para obtener un token de acceso que me permita usar las funcionalidades del sistema.

#### Criterios de Aceptación

1. WHEN un usuario envía una petición POST a `/api/v1/auth/login` con email y password correctos correspondientes a una cuenta activa, THE Sistema_Auth SHALL retornar un AuthResponse con Access_Token y datos del usuario con HTTP 200.
2. WHEN un usuario envía una petición de login con email inexistente o password incorrecto, THE Sistema_Auth SHALL retornar un error HTTP 401 con mensaje genérico "Credenciales inválidas" sin revelar si el email existe o no.
3. WHEN un usuario envía una petición de login con credenciales válidas pero la cuenta está inactiva (active=false), THE Sistema_Auth SHALL retornar un error HTTP 403 con código ACCOUNT_INACTIVE.
4. WHEN un usuario envía una petición de login con email vacío o en formato inválido, THE Sistema_Auth SHALL retornar un error HTTP 400 con detalles de validación.
5. WHEN un usuario envía una petición de login con password vacío, THE Sistema_Auth SHALL retornar un error HTTP 400 con detalles de validación.
6. WHEN el login es exitoso, THE Sistema_Auth SHALL realizar la comparación de email de forma case-insensitive.

### Requisito 4: Gestión de Tokens JWT

**User Story:** Como sistema, necesito generar y validar tokens JWT para mantener sesiones stateless y seguras.

#### Criterios de Aceptación

1. WHEN el Sistema_Auth genera un Access_Token, THE JWT_Service SHALL incluir los claims: sub (userId), email, role, iat (issued at), y exp (expiración entre 15 y 30 minutos desde la emisión).
2. WHEN el JWT_Service valida un token, THE JWT_Service SHALL verificar la firma HMAC y que el token no haya expirado.
3. WHEN el JWT_Service recibe un token con firma inválida o formato malformado, THE JWT_Service SHALL retornar false en la validación.
4. WHEN el JWT_Service recibe un token expirado, THE JWT_Service SHALL identificarlo específicamente como TOKEN_EXPIRED diferenciándolo de otros errores de validación.
5. THE JWT_Service SHALL firmar los tokens con una clave secreta configurable por variable de entorno (app.jwt.secret) que no se almacena en el repositorio.
6. THE JWT_Service SHALL permitir configurar el tiempo de expiración por variable de entorno (app.jwt.expiration-minutes).

### Requisito 5: Filtro de Autenticación JWT

**User Story:** Como sistema, necesito interceptar cada petición HTTP para validar el token y establecer el contexto de seguridad.

#### Criterios de Aceptación

1. WHEN una petición HTTP contiene un header Authorization con formato "Bearer {token}" válido, THE Security_Filter SHALL extraer el userId del token, cargar el User_Entity desde la base de datos, y establecer la autenticación en el SecurityContext con la autoridad ROLE_{role}.
2. WHEN una petición HTTP no contiene header Authorization o no comienza con "Bearer ", THE Security_Filter SHALL permitir que la petición continúe sin autenticación (para que las reglas de SecurityFilterChain decidan si el endpoint es público).
3. WHEN una petición HTTP contiene un token expirado, THE Security_Filter SHALL retornar HTTP 401 con código TOKEN_EXPIRED y mensaje descriptivo en formato JSON.
4. WHEN una petición HTTP contiene un token con firma inválida o formato malformado, THE Security_Filter SHALL retornar HTTP 401 con código UNAUTHORIZED y mensaje descriptivo en formato JSON.
5. WHEN una petición HTTP contiene un token válido pero el usuario no existe en la base de datos o está inactivo, THE Security_Filter SHALL retornar HTTP 401 con código UNAUTHORIZED.

### Requisito 6: Autorización Basada en Roles

**User Story:** Como sistema, necesito restringir el acceso a endpoints según el rol del usuario autenticado, para proteger los recursos del sistema.

#### Criterios de Aceptación

1. THE Security_Filter SHALL configurar los siguientes endpoints como públicos (sin autenticación requerida): `/api/v1/auth/login`, `/api/v1/auth/register/**`, `/api/v1/health`, `/actuator/health`.
2. WHILE un usuario tiene role ADMIN, THE Security_Filter SHALL permitir acceso a todos los endpoints bajo `/api/v1/admin/**`.
3. WHEN un usuario autenticado sin role ADMIN intenta acceder a un endpoint bajo `/api/v1/admin/**`, THE Security_Filter SHALL retornar HTTP 403 Forbidden.
4. WHEN un usuario no autenticado intenta acceder a un endpoint que requiere autenticación, THE Security_Filter SHALL retornar HTTP 401 Unauthorized.
5. THE Security_Filter SHALL usar sesiones stateless (no crear ni mantener HttpSession).
6. THE Security_Filter SHALL deshabilitar CSRF dado que la autenticación es por JWT stateless.

### Requisito 7: Obtener Usuario Autenticado

**User Story:** Como usuario autenticado, quiero obtener mis datos de perfil básico, para confirmar mi identidad y rol en el sistema.

#### Criterios de Aceptación

1. WHEN un usuario autenticado envía una petición GET a `/api/v1/auth/me`, THE Auth_Controller SHALL retornar los datos del usuario (id, name, email, role) con HTTP 200.
2. WHEN un usuario no autenticado envía una petición GET a `/api/v1/auth/me`, THE Security_Filter SHALL retornar HTTP 401.
3. WHEN un usuario autenticado cuya cuenta fue desactivada después de emitir el token envía una petición GET a `/api/v1/auth/me`, THE Security_Filter SHALL retornar HTTP 401.

### Requisito 8: Configuración CORS

**User Story:** Como sistema, necesito permitir peticiones cross-origin desde el frontend para que la aplicación Angular pueda comunicarse con la API.

#### Criterios de Aceptación

1. THE Sistema_Auth SHALL permitir peticiones CORS desde el origen del frontend configurado por variable de entorno.
2. THE Sistema_Auth SHALL permitir los métodos HTTP: GET, POST, PUT, DELETE, OPTIONS.
3. THE Sistema_Auth SHALL permitir los headers: Authorization, Content-Type.
4. THE Sistema_Auth SHALL responder correctamente a peticiones preflight (OPTIONS) sin requerir autenticación.

### Requisito 9: Consolidación de Entidad User

**User Story:** Como equipo de desarrollo, necesitamos usar una única entidad User canónica para evitar duplicación y inconsistencias.

#### Criterios de Aceptación

1. THE Sistema_Auth SHALL usar `py.com.servipy.user.domain.User` como la única entidad de usuario en todo el sistema.
2. THE Sistema_Auth SHALL eliminar o deprecar la clase `py.com.servipy.auth.domain.User` redirigiendo todas las referencias a la entidad canónica.
3. THE Sistema_Auth SHALL asegurar que el UserRepository en el módulo auth apunte a la entidad canónica `py.com.servipy.user.domain.User`.

### Requisito 10: Logout (Client-Side)

**User Story:** Como usuario autenticado, quiero cerrar mi sesión para proteger mi cuenta cuando dejo de usar la aplicación.

#### Criterios de Aceptación

1. WHEN el usuario ejecuta la acción de logout, THE Frontend_AuthService SHALL eliminar el Access_Token del localStorage.
2. WHEN el usuario ejecuta la acción de logout, THE Frontend_AuthService SHALL limpiar el estado del usuario actual (signal a null).
3. WHEN el usuario ejecuta la acción de logout, THE Frontend_AuthService SHALL redirigir al usuario a la página de login.

### Requisito 11: Servicio de Autenticación Frontend

**User Story:** Como desarrollador frontend, necesito un servicio centralizado para gestionar el estado de autenticación en la aplicación Angular.

#### Criterios de Aceptación

1. THE Frontend_AuthService SHALL exponer un método `login(email, password)` que envíe las credenciales al endpoint `/api/v1/auth/login` y almacene el Access_Token en localStorage al recibir respuesta exitosa.
2. THE Frontend_AuthService SHALL exponer un método `register(name, email, password, roleType)` que envíe los datos al endpoint de registro correspondiente al roleType y almacene el Access_Token en localStorage al recibir respuesta exitosa.
3. THE Frontend_AuthService SHALL exponer un método `getToken()` que retorne el Access_Token almacenado en localStorage o null si no existe.
4. THE Frontend_AuthService SHALL exponer un método `isAuthenticated()` que retorne true si existe un Access_Token en localStorage, false en caso contrario.
5. THE Frontend_AuthService SHALL exponer un método `getUserRole()` que retorne el rol del usuario autenticado decodificándolo del token o null si no hay sesión activa.
6. THE Frontend_AuthService SHALL exponer un signal `currentUser` que contenga los datos del usuario autenticado o null.
7. WHEN el login o registro es exitoso, THE Frontend_AuthService SHALL actualizar el signal currentUser con los datos del usuario recibidos en la respuesta.

### Requisito 12: Interceptor JWT Frontend

**User Story:** Como desarrollador frontend, necesito que todas las peticiones HTTP autenticadas incluyan automáticamente el token JWT sin intervención manual.

#### Criterios de Aceptación

1. WHEN el Frontend_AuthService tiene un Access_Token almacenado, THE JWT_Interceptor SHALL agregar el header `Authorization: Bearer {token}` a todas las peticiones HTTP salientes.
2. WHEN una petición HTTP retorna HTTP 401, THE JWT_Interceptor SHALL ejecutar el logout del Frontend_AuthService y redirigir al usuario a la página de login.
3. WHEN el Frontend_AuthService no tiene Access_Token almacenado, THE JWT_Interceptor SHALL enviar las peticiones sin header Authorization.

### Requisito 13: Guards de Navegación

**User Story:** Como desarrollador frontend, necesito proteger rutas para que solo usuarios autenticados con el rol correcto puedan acceder a ciertas secciones.

#### Criterios de Aceptación

1. WHEN un usuario no autenticado intenta navegar a una ruta protegida por Auth_Guard, THE Auth_Guard SHALL redirigir al usuario a la página de login y retornar false.
2. WHEN un usuario autenticado intenta navegar a una ruta protegida por Auth_Guard, THE Auth_Guard SHALL permitir la navegación y retornar true.
3. WHEN un usuario autenticado con un rol diferente al requerido intenta navegar a una ruta protegida por Role_Guard, THE Role_Guard SHALL redirigir al usuario a la página principal y retornar false.
4. WHEN un usuario autenticado con el rol requerido intenta navegar a una ruta protegida por Role_Guard, THE Role_Guard SHALL permitir la navegación y retornar true.
5. THE Role_Guard SHALL aceptar una configuración de roles permitidos a través de los datos de la ruta (route data).

### Requisito 14: Página de Login

**User Story:** Como usuario, quiero una página de inicio de sesión con formulario de email y contraseña, para autenticarme en la aplicación.

#### Criterios de Aceptación

1. THE Página_Login SHALL mostrar un formulario con campos email y password, ambos obligatorios.
2. WHEN el usuario envía el formulario con datos válidos, THE Página_Login SHALL invocar el método login del Frontend_AuthService.
3. WHEN el login es exitoso y el usuario tiene role CLIENT, THE Página_Login SHALL redirigir a `/client`.
4. WHEN el login es exitoso y el usuario tiene role PROFESSIONAL, THE Página_Login SHALL redirigir a `/professional`.
5. WHEN el login es exitoso y el usuario tiene role ADMIN, THE Página_Login SHALL redirigir a `/admin`.
6. WHEN el login falla, THE Página_Login SHALL mostrar un mensaje de error genérico visible para el usuario.
7. WHILE el formulario de login está procesando la petición, THE Página_Login SHALL deshabilitar el botón de envío y mostrar un indicador de carga.
8. THE Página_Login SHALL incluir un enlace de navegación a la página de registro.
9. THE Página_Login SHALL cumplir con accesibilidad básica: labels asociados a inputs, mensajes de error con role="alert", formulario navegable por teclado.

### Requisito 15: Página de Registro

**User Story:** Como visitante, quiero una página de registro con formulario para crear mi cuenta, eligiendo si soy cliente o profesional.

#### Criterios de Aceptación

1. THE Página_Registro SHALL mostrar un formulario con campos name, email, password y un selector de tipo de cuenta (CLIENT o PROFESSIONAL).
2. WHEN el usuario envía el formulario con datos válidos, THE Página_Registro SHALL invocar el método register del Frontend_AuthService con el roleType seleccionado.
3. WHEN el registro es exitoso, THE Página_Registro SHALL redirigir al usuario según su rol (mismas reglas que Requisito 14, criterios 3-5).
4. WHEN el registro falla con error de email duplicado, THE Página_Registro SHALL mostrar un mensaje indicando que el email ya está registrado.
5. WHEN el registro falla con errores de validación, THE Página_Registro SHALL mostrar los mensajes de error correspondientes junto a cada campo.
6. WHILE el formulario de registro está procesando la petición, THE Página_Registro SHALL deshabilitar el botón de envío y mostrar un indicador de carga.
7. THE Página_Registro SHALL validar en frontend que el password tenga mínimo 8 caracteres antes de enviar.
8. THE Página_Registro SHALL incluir un enlace de navegación a la página de login.
9. THE Página_Registro SHALL cumplir con accesibilidad básica: labels asociados a inputs, mensajes de error con role="alert", formulario navegable por teclado.

### Requisito 16: Almacenamiento de Token

**User Story:** Como sistema frontend, necesito persistir el token de acceso para mantener la sesión entre recargas de página.

#### Criterios de Aceptación

1. WHEN el usuario se autentica exitosamente (login o registro), THE Frontend_AuthService SHALL almacenar el Access_Token en localStorage bajo la clave "access_token".
2. WHEN el usuario ejecuta logout o recibe un HTTP 401, THE Frontend_AuthService SHALL eliminar la entrada "access_token" del localStorage.
3. WHEN la aplicación se inicializa, THE Frontend_AuthService SHALL verificar si existe un token en localStorage y restaurar el estado de autenticación si el token existe.
