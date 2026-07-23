# Documento de Requisitos — Fundación del Proyecto y Autenticación

## Introducción

Este documento define los requisitos para la capa fundacional del proyecto ServiPy y el sistema de autenticación. Cubre la estructura del monorepo, la configuración local, el endpoint de salud, el registro de usuarios (clientes y profesionales), el inicio de sesión, la obtención de información del usuario autenticado, la autenticación basada en JWT, los roles del sistema y los guards de rutas en el frontend. No incluye perfiles profesionales, búsquedas, servicios ofrecidos, solicitudes, mapas, WhatsApp ni dashboards.

## Glosario

- **Sistema**: La aplicación ServiPy en su conjunto (frontend + backend).
- **Backend**: El servidor Spring Boot que expone la API REST bajo el prefijo `/api/v1`.
- **Frontend**: La aplicación Angular con Tailwind CSS que consume la API.
- **Monorepo**: Repositorio único que contiene los directorios `frontend/` y `backend/`.
- **JWT**: JSON Web Token utilizado para autenticación stateless.
- **Guard_de_Ruta**: Componente Angular que restringe el acceso a rutas según el rol del usuario autenticado.
- **Usuario**: Entidad con id, name, email, passwordHash, role, active, createdAt, updatedAt.
- **Rol**: Enumeración con valores CLIENT, PROFESSIONAL o ADMIN, asignado exactamente uno por usuario.
- **DTO**: Data Transfer Object; estructura utilizada para transferir datos sin exponer entidades JPA.
- **Health_Endpoint**: Endpoint GET /api/v1/health que indica el estado del servicio.
- **Token_de_Acceso**: JWT emitido tras un login exitoso, enviado en el encabezado `Authorization: Bearer <token>`.
- **Admin_Semilla**: Usuario administrador precargado en la base de datos, no registrado vía API.
- **Error_Uniforme**: Estructura JSON estándar de error con campos timestamp, status, code, message y errors[].

## Requisitos

### Requisito 1: Estructura del Monorepo

**Historia de Usuario:** Como desarrollador del equipo, quiero que el proyecto tenga una estructura de monorepo bien definida, para que frontend y backend convivan en un solo repositorio con separación clara.

#### Criterios de Aceptación

1. THE Monorepo SHALL contener un directorio `frontend/` con un proyecto Angular que compile sin errores mediante `ng build` y que tenga Tailwind CSS integrado de modo que las clases utilitarias de Tailwind se resuelvan en el CSS de salida.
2. THE Monorepo SHALL contener un directorio `backend/` con un proyecto Spring Boot que compile sin errores mediante `./mvnw compile` (o `./gradlew compileJava`) y que incluya la dependencia del conector MySQL y la configuración de datasource en sus archivos de propiedades.
3. THE Monorepo SHALL contener un directorio `docs/` con al menos los siguientes documentos: MVP, contrato de API, modelo de dominio, estrategia de ramas y definición de terminado.
4. THE Monorepo SHALL contener un directorio `.kiro/` con los subdirectorios `steering/` y `specs/`.
5. THE Monorepo SHALL contener un directorio `database/` para scripts de base de datos y un directorio `.github/` para configuración de GitHub.
6. THE Monorepo SHALL contener un archivo `README.md` en la raíz que describa la estructura de directorios, las tecnologías utilizadas y los enlaces a la documentación en `docs/`.

### Requisito 2: Configuración Local mediante Variables de Entorno

**Historia de Usuario:** Como desarrollador del equipo, quiero que la configuración sensible se maneje mediante variables de entorno, para que no se almacenen secretos en el código fuente.

#### Criterios de Aceptación

1. THE Backend SHALL leer la conexión a la base de datos (URL, usuario y contraseña) desde variables de entorno independientes.
2. THE Backend SHALL leer la clave secreta para firmar JWT desde una variable de entorno.
3. THE Backend SHALL leer el tiempo de expiración del JWT desde una variable de entorno, interpretando el valor como cantidad de minutos.
4. IF una variable de entorno requerida no está definida o su valor está vacío, THEN THE Backend SHALL fallar al iniciar con un mensaje indicando el nombre de la variable faltante o vacía.
5. THE Monorepo SHALL incluir un archivo `.env.example` que liste todas las variables de entorno requeridas con valores de ejemplo no funcionales y un comentario descriptivo por cada una.
6. IF una variable de entorno requerida contiene un valor con formato inválido (por ejemplo, URL malformada o valor no numérico para la expiración), THEN THE Backend SHALL fallar al iniciar con un mensaje indicando la variable y el formato esperado.
7. THE Backend SHALL validar todas las variables de entorno requeridas durante el arranque de la aplicación, antes de aceptar conexiones entrantes.

### Requisito 3: Endpoint de Salud

**Historia de Usuario:** Como desarrollador o sistema de monitoreo, quiero un endpoint de verificación de salud, para poder confirmar que el backend está operativo.

#### Criterios de Aceptación

1. WHEN se recibe una solicitud GET en `/api/v1/health`, THE Backend SHALL responder en un máximo de 2000 ms con código HTTP 200, encabezado `Content-Type: application/json` y un cuerpo JSON `{"status": "UP"}`.
2. THE Health_Endpoint SHALL estar accesible sin incluir encabezado `Authorization` ni ningún otro mecanismo de autenticación.
3. IF se recibe una solicitud con un método HTTP distinto de GET en `/api/v1/health`, THEN THE Backend SHALL responder con código HTTP 405 (Method Not Allowed).

### Requisito 4: Registro de Cliente

**Historia de Usuario:** Como persona que necesita contratar servicios, quiero registrarme como cliente, para poder acceder a la plataforma.

#### Criterios de Aceptación

1. WHEN se recibe una solicitud POST en `/api/v1/auth/register/client` con los campos name, email y password válidos, THE Backend SHALL crear un Usuario con rol CLIENT, active en true, y responder con código HTTP 201 incluyendo un objeto JSON con accessToken, tokenType y los datos públicos del usuario creado (id, name, email, role).
2. WHEN se recibe una solicitud de registro de cliente, THE Backend SHALL almacenar la contraseña utilizando bcrypt y no incluir el hash en la respuesta.
3. IF el campo email ya está registrado en el sistema (comparación sin distinción de mayúsculas/minúsculas), THEN THE Backend SHALL responder con código HTTP 409 y un Error_Uniforme con code `DUPLICATE_EMAIL`.
4. IF algún campo requerido (name, email, password) está ausente o tiene formato inválido, THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `VALIDATION_ERROR` que incluya en el array errors cada campo con error y su mensaje descriptivo.
5. THE Backend SHALL validar que el campo email tenga formato de correo electrónico válido (contenga exactamente un carácter @, un dominio con al menos un punto, y no exceda 255 caracteres).
6. THE Backend SHALL validar que el campo password tenga entre 8 y 72 caracteres (límite máximo de bcrypt).
7. THE Backend SHALL validar que el campo name tenga entre 2 y 100 caracteres, sin considerar espacios en blanco iniciales o finales.

### Requisito 5: Registro de Profesional

**Historia de Usuario:** Como persona que ofrece servicios profesionales, quiero registrarme como profesional, para poder publicar mis servicios en la plataforma una vez aprobado.

#### Criterios de Aceptación

1. WHEN se recibe una solicitud POST en `/api/v1/auth/register/professional` con los campos name, email y password válidos, THE Backend SHALL crear un Usuario con rol PROFESSIONAL y estado active=true, y responder con código HTTP 201 incluyendo los campos id, name, email y role del usuario creado.
2. WHEN se registra un profesional, THE Backend SHALL almacenar la contraseña utilizando un algoritmo de hashing seguro (bcrypt).
3. IF el campo email ya está registrado en el sistema, THEN THE Backend SHALL responder con código HTTP 409 y un Error_Uniforme con code `DUPLICATE_EMAIL`.
4. IF algún campo requerido está ausente o tiene formato inválido, THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `VALIDATION_ERROR` que incluya los campos con errores.
5. THE Backend SHALL validar que el campo email tenga formato de correo electrónico válido según la estructura `local@dominio.extensión` y no exceda 255 caracteres.
6. THE Backend SHALL validar que el campo password tenga al menos 8 caracteres y no más de 72 caracteres.
7. THE Backend SHALL validar que el campo name no esté vacío, tenga al menos 2 caracteres y no más de 100 caracteres.

### Requisito 6: Inicio de Sesión

**Historia de Usuario:** Como usuario registrado (cliente, profesional o administrador), quiero iniciar sesión con mi email y contraseña, para obtener un token de acceso que me permita usar la plataforma.

#### Criterios de Aceptación

1. WHEN se recibe una solicitud POST en `/api/v1/auth/login` con email y password correctos, THE Backend SHALL responder con código HTTP 200 y un cuerpo JSON con los campos accessToken, tokenType ("Bearer") y un objeto user con id, name, email y role.
2. IF el campo email está ausente, vacío o no tiene formato de correo electrónico válido, o el campo password está ausente o vacío, THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `VALIDATION_ERROR` indicando los campos inválidos.
3. IF el email no está registrado en el sistema, THEN THE Backend SHALL responder con código HTTP 401 y un Error_Uniforme con code `INVALID_CREDENTIALS`.
4. IF la contraseña proporcionada no coincide con el hash almacenado, THEN THE Backend SHALL responder con código HTTP 401 y un Error_Uniforme con code `INVALID_CREDENTIALS`.
5. IF la cuenta del usuario está inactiva (campo active es false), THEN THE Backend SHALL responder con código HTTP 403 y un Error_Uniforme con code `ACCOUNT_INACTIVE`.
6. WHEN el login es exitoso, THE Backend SHALL generar un Token_de_Acceso JWT firmado con la clave secreta configurada por variable de entorno, que contenga el id del usuario, el email, el rol, y un tiempo de expiración según la variable de entorno configurada.
7. IF la variable de entorno de la clave secreta JWT no está configurada, THEN THE Backend SHALL rechazar el inicio del servicio e impedir la generación de tokens.

### Requisito 7: Obtener Información del Usuario Autenticado

**Historia de Usuario:** Como usuario autenticado, quiero consultar mi información actual, para que el frontend pueda mostrar mis datos y verificar mi sesión.

#### Criterios de Aceptación

1. WHEN se recibe una solicitud GET en `/api/v1/auth/me` con un Token_de_Acceso válido (firma verificada, no expirado y asociado a un usuario con campo active=true) en el encabezado Authorization con formato `Bearer <token>`, THE Backend SHALL responder con código HTTP 200 y un cuerpo JSON con los campos id (entero), name (cadena), email (cadena) y role (uno de CLIENT, PROFESSIONAL, ADMIN) del usuario.
2. IF la solicitud no incluye un Token_de_Acceso en el encabezado Authorization, o el token está malformado, o su firma no puede verificarse, THEN THE Backend SHALL responder con código HTTP 401 y un Error_Uniforme con code `UNAUTHORIZED`.
3. IF el Token_de_Acceso ha expirado, THEN THE Backend SHALL responder con código HTTP 401 y un Error_Uniforme con code `TOKEN_EXPIRED`.
4. IF el Token_de_Acceso es válido pero el usuario asociado tiene el campo active=false, THEN THE Backend SHALL responder con código HTTP 401 y un Error_Uniforme con code `UNAUTHORIZED`.

### Requisito 8: Autenticación JWT

**Historia de Usuario:** Como sistema, quiero proteger los endpoints con autenticación basada en JWT, para que solo usuarios autenticados accedan a recursos protegidos.

#### Criterios de Aceptación

1. THE Backend SHALL validar el Token_de_Acceso en cada solicitud a endpoints protegidos extrayéndolo del encabezado `Authorization: Bearer <token>`, verificando la firma con la clave secreta y comprobando que no haya expirado.
2. IF un endpoint protegido recibe una solicitud sin encabezado Authorization, con un token malformado, con firma inválida, o con un token cuyo usuario tiene active=false, THEN THE Backend SHALL responder con código HTTP 401 y un Error_Uniforme con code `UNAUTHORIZED`.
3. IF un endpoint protegido recibe una solicitud con un token expirado, THEN THE Backend SHALL responder con código HTTP 401 y un Error_Uniforme con code `TOKEN_EXPIRED`.
4. THE Backend SHALL utilizar DTOs para todas las respuestas de la API, sin exponer entidades JPA directamente.
5. THE Backend SHALL incluir en el JWT los claims: sub (userId), email, role y exp (expiración).
6. THE Backend SHALL devolver fechas en formato ISO 8601 en todas las respuestas.
7. THE Backend SHALL excluir de la validación JWT los endpoints públicos: `/api/v1/health`, `/api/v1/auth/login`, `/api/v1/auth/register/client` y `/api/v1/auth/register/professional`.

### Requisito 9: Roles y Autorización

**Historia de Usuario:** Como sistema, quiero definir tres roles con permisos diferenciados, para que cada tipo de usuario acceda solo a las funcionalidades que le corresponden.

#### Criterios de Aceptación

1. THE Sistema SHALL soportar exactamente tres roles: CLIENT, PROFESSIONAL y ADMIN.
2. WHEN un usuario se registra mediante `/api/v1/auth/register/client`, THE Backend SHALL asignar el rol CLIENT; WHEN un usuario se registra mediante `/api/v1/auth/register/professional`, THE Backend SHALL asignar el rol PROFESSIONAL. THE Backend SHALL crear cuentas con rol ADMIN únicamente mediante seed de datos y no ofrecerá endpoint público de registro para dicho rol.
3. IF un usuario no autenticado intenta acceder a un recurso que requiere autenticación, THEN THE Backend SHALL responder con código HTTP 401 y un Error_Uniforme con code `UNAUTHORIZED`. IF un usuario autenticado intenta acceder a un recurso para el cual su rol no tiene permiso, THEN THE Backend SHALL responder con código HTTP 403 y un Error_Uniforme con code `FORBIDDEN`.
4. THE Backend SHALL permitir que los endpoints de registro (`/api/v1/auth/register/client` y `/api/v1/auth/register/professional`) sean accesibles sin autenticación.
5. THE Backend SHALL permitir que el endpoint de login (`/api/v1/auth/login`) y el endpoint de salud (`/api/v1/health`) sean accesibles sin autenticación.
6. THE Backend SHALL aplicar la siguiente matriz de permisos: `/api/v1/auth/me` accesible para CLIENT, PROFESSIONAL y ADMIN; endpoints bajo `/api/v1/admin/*` accesibles únicamente para ADMIN.

### Requisito 10: Guards de Rutas en el Frontend

**Historia de Usuario:** Como usuario del frontend, quiero que las rutas estén protegidas según mi rol, para no acceder accidentalmente a secciones que no me corresponden.

#### Criterios de Aceptación

1. WHEN un usuario no autenticado intenta acceder a una ruta protegida, THE Frontend SHALL redirigir al usuario a la ruta `/login`.
2. WHEN un usuario autenticado con rol R intenta acceder a una ruta restringida a un rol diferente, THE Frontend SHALL redirigir al usuario a su página principal según su rol (CLIENT → `/client`, PROFESSIONAL → `/professional`, ADMIN → `/admin`).
3. THE Frontend SHALL almacenar el Token_de_Acceso en `localStorage` bajo la clave `accessToken`.
4. THE Frontend SHALL incluir el Token_de_Acceso en el encabezado `Authorization: Bearer <token>` en cada solicitud HTTP a endpoints protegidos mediante un HttpInterceptor de Angular.
5. WHEN el backend responde con código HTTP 401, THE Frontend SHALL eliminar el Token_de_Acceso del almacenamiento, limpiar el estado del usuario en memoria y redirigir a `/login`.
6. THE Frontend SHALL definir al menos tres grupos de rutas protegidas: rutas de cliente (accesibles solo con rol CLIENT), rutas de profesional (accesibles solo con rol PROFESSIONAL) y rutas de administrador (accesibles solo con rol ADMIN).

### Requisito 11: Usuario Administrador Semilla

**Historia de Usuario:** Como equipo de desarrollo, quiero que exista un usuario administrador precargado, para poder gestionar la plataforma sin necesidad de un registro público de administradores.

#### Criterios de Aceptación

1. WHEN el Backend inicia y la base de datos no contiene un Usuario cuyo email coincida con el valor de la variable de entorno `ADMIN_EMAIL`, THE Backend SHALL crear un Usuario con rol ADMIN, estado active=true, y los campos name, email y password tomados de las variables de entorno `ADMIN_NAME`, `ADMIN_EMAIL` y `ADMIN_PASSWORD` respectivamente.
2. THE Admin_Semilla SHALL tener su contraseña almacenada con hash bcrypt, igual que cualquier otro usuario.
3. THE Backend SHALL no exponer ningún endpoint público para registrar usuarios con rol ADMIN.
4. IF el Admin_Semilla ya existe en la base de datos (detectado por coincidencia de email con `ADMIN_EMAIL`), THEN THE Backend SHALL omitir la creación y continuar el arranque sin generar error.
5. IF alguna de las variables de entorno requeridas (`ADMIN_NAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`) está ausente o vacía al iniciar, THEN THE Backend SHALL abortar el arranque y registrar un mensaje de error indicando la variable faltante.

### Requisito 12: Respuestas de Error Uniformes

**Historia de Usuario:** Como desarrollador del frontend, quiero que todos los errores del backend tengan una estructura consistente, para poder manejarlos de forma uniforme en la interfaz.

#### Criterios de Aceptación

1. THE Backend SHALL devolver todos los errores con la estructura Error_Uniforme conteniendo los campos: timestamp (ISO 8601), status (código HTTP numérico), code (cadena en mayúsculas), message (descripción en español) y errors[] (lista de errores de campo, puede estar vacía).
2. WHEN ocurre un error de validación, THE Backend SHALL incluir en el array errors[] un objeto por cada campo inválido con los atributos field (nombre del campo tal como se envió en la solicitud) y message (razón del rechazo en español).
3. IF ocurre un error inesperado del servidor, THEN THE Backend SHALL responder con código HTTP 500 y un Error_Uniforme con code `INTERNAL_ERROR` cuyo campo message no contenga nombres de clases, trazas de pila, consultas a base de datos ni rutas internas del sistema.
4. IF el cuerpo de la solicitud no es JSON válido o no puede ser parseado, THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `MALFORMED_REQUEST` y el array errors[] vacío.
5. WHEN un recurso solicitado no existe, THE Backend SHALL responder con código HTTP 404 y un Error_Uniforme con code `RESOURCE_NOT_FOUND` y el array errors[] vacío.

### Requisito 13: Pruebas Mínimas de Autenticación y Autorización

**Historia de Usuario:** Como equipo de desarrollo, quiero que existan pruebas automatizadas para los flujos de autenticación y autorización, para garantizar que los cambios futuros no rompan la seguridad del sistema.

#### Criterios de Aceptación

1. THE Backend SHALL incluir una prueba que envíe POST a `/api/v1/auth/register/client` con name, email y password válidos y verifique que la respuesta tenga estado HTTP 201 y contenga los campos accessToken, tokenType y user con el rol CLIENT.
2. THE Backend SHALL incluir una prueba que intente registrar un cliente con un email ya existente en el sistema y verifique que la respuesta tenga estado HTTP 409 con code `DUPLICATE_EMAIL`.
3. THE Backend SHALL incluir una prueba que envíe POST a `/api/v1/auth/login` con email y password de un usuario previamente registrado y verifique que la respuesta tenga estado HTTP 200 y contenga accessToken, tokenType y el objeto user con id, name, email y role.
4. THE Backend SHALL incluir una prueba que envíe POST a `/api/v1/auth/login` con un email existente pero password incorrecto y verifique que la respuesta tenga estado HTTP 401, y otra prueba con un email no registrado que también devuelva estado HTTP 401, sin revelar cuál de los dos campos es incorrecto.
5. THE Backend SHALL incluir una prueba que envíe una solicitud a GET `/api/v1/auth/me` sin header Authorization y verifique que la respuesta tenga estado HTTP 401.
6. THE Backend SHALL incluir una prueba que envíe una solicitud a GET `/api/v1/auth/me` con un token JWT expirado en el header Authorization y verifique que la respuesta tenga estado HTTP 401.
7. THE Backend SHALL incluir una prueba que autentique un usuario con rol CLIENT e intente acceder a GET `/api/v1/admin/professionals/pending` y verifique que la respuesta tenga estado HTTP 403.
