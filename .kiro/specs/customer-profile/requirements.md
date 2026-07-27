# Documento de Requisitos — Perfil de Cliente

## Introducción

Este documento define los requisitos para la gestión del perfil de un cliente autenticado en la plataforma ServiPy. Cubre la consulta del perfil, la edición de datos personales, el cambio de foto de perfil, el cambio de contraseña y la consulta del historial de solicitudes enviadas. No incluye autenticación (ya cubierta en otro spec), administración, catálogo de profesionales ni pagos.

## Glosario

- **Sistema**: La aplicación ServiPy en su conjunto (frontend + backend).
- **Backend**: El servidor Spring Boot que expone la API REST bajo el prefijo `/api/v1`.
- **Frontend**: La aplicación Angular con Tailwind CSS que consume la API.
- **Cliente**: Usuario autenticado con rol CLIENT.
- **Perfil_de_Cliente**: Conjunto de datos personales del cliente: nombre, email, teléfono y foto de perfil.
- **Foto_de_Perfil**: Imagen asociada al cliente, almacenada como URL accesible públicamente.
- **Solicitud**: Petición de servicio enviada por un cliente a un profesional, con campos: id, servicio solicitado, profesional destino, estado, fecha de creación y última actualización.
- **Estado_de_Solicitud**: Enumeración con valores posibles: PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED.
- **Token_de_Acceso**: JWT emitido tras login exitoso, enviado en el encabezado `Authorization: Bearer <token>`.
- **Error_Uniforme**: Estructura JSON estándar de error con campos timestamp, status, code, message y errors[].
- **DTO**: Data Transfer Object; estructura utilizada para transferir datos sin exponer entidades JPA.

## Requisitos

### Requisito 1: Consultar Perfil del Cliente

**Historia de Usuario:** Como cliente autenticado, quiero consultar mi perfil, para poder ver mis datos personales actuales en la plataforma.

#### Criterios de Aceptación

1. WHEN se recibe una solicitud GET en `/api/v1/client/profile` con un Token_de_Acceso válido de un Cliente, THE Backend SHALL responder con código HTTP 200 y un cuerpo JSON con los campos id (entero), name (cadena, entre 1 y 100 caracteres), email (cadena, entre 1 y 255 caracteres), phone (cadena de entre 1 y 20 caracteres, o null) y photoUrl (cadena de entre 1 y 500 caracteres, o null).
2. IF la solicitud no incluye un Token_de_Acceso o el token es inválido (expirado, malformado o con firma no reconocida), THEN THE Backend SHALL responder con código HTTP 401 y un Error_Uniforme con code `UNAUTHORIZED`.
3. IF el Token_de_Acceso es válido pero pertenece a un usuario con rol diferente de CLIENT, THEN THE Backend SHALL responder con código HTTP 403 y un Error_Uniforme con code `FORBIDDEN`.
4. IF el Token_de_Acceso es válido y pertenece a un Cliente pero no se encuentra un perfil asociado al identificador del token, THEN THE Backend SHALL responder con código HTTP 404 y un Error_Uniforme con code `NOT_FOUND`.
5. THE Backend SHALL utilizar un DTO para la respuesta del perfil, sin exponer la entidad JPA User directamente.

### Requisito 2: Editar Datos Personales del Cliente

**Historia de Usuario:** Como cliente autenticado, quiero editar mis datos personales (nombre y teléfono), para mantener mi información actualizada en la plataforma.

#### Criterios de Aceptación

1. WHEN se recibe una solicitud PUT en `/api/v1/client/profile` con un Token_de_Acceso válido de un Cliente y un cuerpo JSON con los campos name y phone, THE Backend SHALL actualizar los datos del cliente, actualizar el campo updatedAt con la fecha y hora actual, y responder con código HTTP 200 y el perfil actualizado (id, name, email, phone, photoUrl, updatedAt).
2. IF el campo name está ausente, vacío, tiene menos de 2 caracteres o más de 100 caracteres (sin considerar espacios en blanco iniciales o finales), THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `VALIDATION_ERROR` indicando el campo name y la razón del rechazo.
3. IF el campo phone está presente y tiene más de 20 caracteres o contiene caracteres no válidos (solo se permiten dígitos, espacios, guiones y el prefijo +), THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `VALIDATION_ERROR` indicando el campo phone y la razón del rechazo.
4. IF la solicitud no incluye un Token_de_Acceso válido, THEN THE Backend SHALL responder con código HTTP 401 y un Error_Uniforme con code `UNAUTHORIZED`.
5. IF el Token_de_Acceso pertenece a un usuario con rol diferente de CLIENT, THEN THE Backend SHALL responder con código HTTP 403 y un Error_Uniforme con code `FORBIDDEN`.
6. THE Backend SHALL ignorar cualquier intento de modificar los campos email, role o id mediante este endpoint, preservando sus valores originales sin generar error.
7. IF el campo phone está presente y tiene menos de 7 caracteres (sin considerar espacios en blanco iniciales o finales), THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `VALIDATION_ERROR` indicando el campo phone y la razón del rechazo.
8. IF el cuerpo de la solicitud no es un JSON válido o está vacío, THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `VALIDATION_ERROR` indicando que el cuerpo de la solicitud es inválido.
9. WHEN se recibe una solicitud PUT en `/api/v1/client/profile` con un Token_de_Acceso válido de un Cliente y el campo phone es null o está ausente en el cuerpo JSON, THE Backend SHALL almacenar el valor de phone como null y responder con código HTTP 200 y el perfil actualizado.

### Requisito 3: Cambiar Foto de Perfil del Cliente

**Historia de Usuario:** Como cliente autenticado, quiero cambiar mi foto de perfil, para personalizar mi cuenta y que los profesionales me identifiquen.

#### Criterios de Aceptación

1. WHEN se recibe una solicitud PUT en `/api/v1/client/profile/photo` con un Token_de_Acceso válido de un Cliente y un cuerpo multipart con un archivo de imagen válido (tipo MIME permitido y tamaño no superior a 5 MB), THE Backend SHALL almacenar la imagen, actualizar el campo photoUrl del cliente con la URL de acceso público a la imagen, actualizar el campo updatedAt con la fecha y hora del servidor en formato ISO 8601, y responder con código HTTP 200 y un cuerpo JSON con el campo photoUrl conteniendo la nueva URL.
2. THE Backend SHALL aceptar únicamente archivos con tipo MIME `image/jpeg`, `image/png` o `image/webp`, validando el contenido real del archivo además del encabezado Content-Type declarado.
3. IF el archivo tiene un tipo MIME no permitido, THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `INVALID_FILE_TYPE` y un mensaje indicando los tipos permitidos.
4. IF el archivo excede 5 MB de tamaño, THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `FILE_TOO_LARGE` y un mensaje indicando el tamaño máximo permitido de 5 MB.
5. IF la solicitud no incluye un archivo en el campo multipart, THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `VALIDATION_ERROR` indicando que el archivo es requerido.
6. IF la solicitud no incluye un Token_de_Acceso válido, THEN THE Backend SHALL responder con código HTTP 401 y un Error_Uniforme con code `UNAUTHORIZED`.
7. IF el Token_de_Acceso pertenece a un usuario con rol diferente de CLIENT, THEN THE Backend SHALL responder con código HTTP 403 y un Error_Uniforme con code `FORBIDDEN`.
8. WHEN el cliente ya tiene una foto de perfil anterior (photoUrl no es null), THE Backend SHALL reemplazar la referencia anterior con la nueva URL sin eliminar el archivo anterior del almacenamiento (la limpieza se manejará por proceso separado).
9. IF el almacenamiento de la imagen falla, THEN THE Backend SHALL responder con código HTTP 500 y un Error_Uniforme con code `INTERNAL_ERROR` y un mensaje indicando que no se pudo almacenar la imagen, sin modificar el photoUrl ni el updatedAt existentes del cliente.
10. IF el archivo tiene un tamaño de 0 bytes, THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `VALIDATION_ERROR` indicando que el archivo está vacío.

### Requisito 4: Cambiar Contraseña del Cliente

**Historia de Usuario:** Como cliente autenticado, quiero cambiar mi contraseña, para mantener la seguridad de mi cuenta.

#### Criterios de Aceptación

1. WHEN se recibe una solicitud PUT en `/api/v1/client/profile/password` con un Token_de_Acceso válido de un Cliente y un cuerpo JSON con los campos currentPassword y newPassword, THE Backend SHALL verificar que currentPassword coincida con el hash almacenado, actualizar el hash de la contraseña con newPassword utilizando bcrypt, actualizar el campo updatedAt, y responder con código HTTP 200 y un cuerpo JSON con el campo message indicando que la contraseña fue actualizada exitosamente.
2. IF el campo currentPassword no coincide con el hash almacenado, THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `INVALID_CURRENT_PASSWORD`.
3. IF el campo newPassword tiene menos de 8 caracteres o más de 72 caracteres (límite de bcrypt), THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `VALIDATION_ERROR` indicando el campo newPassword y la razón del rechazo.
4. IF el campo currentPassword o newPassword está ausente o vacío, THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `VALIDATION_ERROR` indicando los campos faltantes.
5. IF la solicitud no incluye un Token_de_Acceso válido, THEN THE Backend SHALL responder con código HTTP 401 y un Error_Uniforme con code `UNAUTHORIZED`.
6. IF el Token_de_Acceso pertenece a un usuario con rol diferente de CLIENT, THEN THE Backend SHALL responder con código HTTP 403 y un Error_Uniforme con code `FORBIDDEN`.
7. THE Backend SHALL no incluir el hash de la contraseña en ninguna respuesta.
8. IF el campo newPassword no contiene al menos una letra mayúscula, una letra minúscula y un dígito, THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `VALIDATION_ERROR` indicando el campo newPassword y la razón del rechazo.
9. IF el campo newPassword es idéntico al campo currentPassword, THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `VALIDATION_ERROR` indicando que la nueva contraseña debe ser diferente a la actual.
10. WHEN la contraseña se actualiza exitosamente, THE Backend SHALL invalidar todos los Token_de_Acceso emitidos previamente para ese Cliente, excepto el token utilizado en la solicitud actual.

### Requisito 5: Consultar Historial de Solicitudes Enviadas

**Historia de Usuario:** Como cliente autenticado, quiero consultar el historial de solicitudes que he enviado, para dar seguimiento al estado de mis peticiones de servicio.

#### Criterios de Aceptación

1. WHEN se recibe una solicitud GET en `/api/v1/client/requests` con un Token_de_Acceso válido de un Cliente, THE Backend SHALL responder con código HTTP 200 y un cuerpo JSON con un array de objetos, cada uno conteniendo los campos: id (entero), serviceName (cadena, máximo 100 caracteres), professionalName (cadena, máximo 150 caracteres), status (uno de PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED), createdAt (ISO 8601 con zona horaria UTC) y updatedAt (ISO 8601 con zona horaria UTC).
2. THE Backend SHALL devolver únicamente las solicitudes pertenecientes al cliente autenticado identificado por el Token_de_Acceso, sin exponer solicitudes de otros clientes.
3. THE Backend SHALL ordenar las solicitudes por fecha de creación descendente (más recientes primero).
4. WHEN el cliente autenticado no tiene solicitudes registradas, THE Backend SHALL responder con código HTTP 200 y un array JSON vacío `[]`.
5. IF la solicitud no incluye un Token_de_Acceso o el token es inválido (expirado, malformado o con firma incorrecta), THEN THE Backend SHALL responder con código HTTP 401 y un Error_Uniforme con code `UNAUTHORIZED`.
6. IF el Token_de_Acceso pertenece a un usuario con rol diferente de CLIENT, THEN THE Backend SHALL responder con código HTTP 403 y un Error_Uniforme con code `FORBIDDEN`.
7. WHERE se incluye el parámetro de consulta `status` con un valor válido de Estado_de_Solicitud (PENDING, ACCEPTED, REJECTED, COMPLETED o CANCELLED, sin distinción de mayúsculas/minúsculas), THE Backend SHALL filtrar las solicitudes devolviendo únicamente aquellas cuyo estado coincida con el valor indicado.
8. IF el parámetro de consulta `status` contiene un valor que no corresponde a ningún Estado_de_Solicitud válido, THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `VALIDATION_ERROR` y un mensaje indicando los valores permitidos: PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED.
9. WHEN se recibe una solicitud GET en `/api/v1/client/requests` y el cliente autenticado tiene más de 50 solicitudes, THE Backend SHALL aplicar paginación por defecto devolviendo las primeras 20 solicitudes junto con metadatos de paginación: totalElements (entero), totalPages (entero), currentPage (entero, base 0) y pageSize (entero, por defecto 20, máximo 100).
10. IF se proporcionan parámetros de paginación `page` o `size` con valores no numéricos o fuera de rango (page menor a 0, size menor a 1 o mayor a 100), THEN THE Backend SHALL responder con código HTTP 400 y un Error_Uniforme con code `VALIDATION_ERROR` indicando los rangos permitidos.

### Requisito 6: Interfaz de Perfil del Cliente en el Frontend

**Historia de Usuario:** Como cliente autenticado, quiero una interfaz visual para gestionar mi perfil, para poder realizar todas las operaciones de perfil de forma intuitiva.

#### Criterios de Aceptación

1. WHILE el Cliente está autenticado, THE Frontend SHALL presentar una página de perfil accesible desde la ruta `/client/profile` que muestre los datos actuales del cliente (nombre, email, teléfono y foto de perfil) dentro de los 2 segundos posteriores a la navegación.
2. THE Frontend SHALL presentar un formulario de edición de datos personales utilizando Reactive Forms de Angular con validaciones en tiempo real para los campos name (requerido, 2-100 caracteres) y phone (opcional, máximo 20 caracteres, solo dígitos, espacios, guiones y prefijo +), mostrando el mensaje de validación correspondiente debajo del campo dentro de los 300 milisegundos posteriores a la pérdida de foco del campo.
3. THE Frontend SHALL presentar un componente de carga de foto que permita seleccionar un archivo de imagen, muestre una vista previa antes de confirmar, y limite los tipos aceptados a JPEG, PNG y WebP con tamaño máximo de 5 MB.
4. IF el cliente selecciona un archivo con tipo distinto a JPEG, PNG o WebP, o con tamaño superior a 5 MB, THEN THE Frontend SHALL mostrar una notificación de error indicando la restricción violada y no enviar el archivo al backend.
5. THE Frontend SHALL presentar un formulario de cambio de contraseña con los campos contraseña actual y nueva contraseña, validando que la nueva contraseña tenga entre 8 y 72 caracteres, y que ambos campos sean requeridos antes de habilitar el envío.
6. WHEN una operación de perfil (edición de datos, carga de foto o cambio de contraseña) se completa exitosamente, THE Frontend SHALL mostrar una notificación de éxito visible durante al menos 3 segundos.
7. THE Frontend SHALL deshabilitar el botón de envío de cada formulario mientras la solicitud HTTP esté en curso, evitando envíos duplicados, y restaurar el estado habilitado del botón una vez recibida la respuesta del backend.
8. WHEN el backend responde con un Error_Uniforme, THE Frontend SHALL extraer el campo message y mostrarlo como notificación de error visible durante al menos 3 segundos.
9. IF la solicitud HTTP no recibe respuesta del backend en un plazo de 15 segundos, THEN THE Frontend SHALL cancelar la solicitud, restaurar el estado habilitado del botón de envío y mostrar una notificación de error indicando que la operación no pudo completarse por tiempo de espera agotado.
10. IF el Cliente no está autenticado y navega a `/client/profile`, THEN THE Frontend SHALL redirigir al Cliente a la página de inicio de sesión sin mostrar la página de perfil.

### Requisito 7: Interfaz de Historial de Solicitudes en el Frontend

**Historia de Usuario:** Como cliente autenticado, quiero una interfaz visual para consultar mi historial de solicitudes, para ver el estado de cada petición que he realizado.

#### Criterios de Aceptación

1. WHEN el Cliente navega a la ruta `/client/requests`, THE Frontend SHALL presentar una página de historial de solicitudes que muestre una lista con las solicitudes del cliente ordenadas por fecha de creación descendente, mostrando un máximo de 50 solicitudes por página.
2. THE Frontend SHALL mostrar para cada solicitud en la lista: nombre del servicio, nombre del profesional, estado representado con un indicador visual de color diferenciado para cada valor de Estado_de_Solicitud (PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED), fecha de creación formateada en formato `dd/MM/yyyy HH:mm` y fecha de última actualización formateada en el mismo formato.
3. THE Frontend SHALL presentar un filtro por estado que permita seleccionar un único valor de Estado_de_Solicitud (PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED) o la opción "Todas" para ver todas las solicitudes, y al cambiar la selección la lista se actualizará en un máximo de 1 segundo.
4. WHEN la lista de solicitudes resultante está vacía (ya sea por ausencia de solicitudes o por el filtro aplicado), THE Frontend SHALL mostrar un mensaje indicando que no se encontraron solicitudes.
5. WHILE se obtienen las solicitudes del backend, THE Frontend SHALL mostrar un indicador de carga visible y deshabilitar la interacción con el filtro de estado hasta que la respuesta se reciba o transcurran 30 segundos.
6. IF la solicitud al backend falla o no responde en un máximo de 30 segundos, THEN THE Frontend SHALL ocultar el indicador de carga y mostrar un mensaje de error indicando que no se pudieron cargar las solicitudes, junto con una opción para reintentar la carga.
7. IF el usuario no está autenticado como Cliente al acceder a `/client/requests`, THEN THE Frontend SHALL redirigir al usuario a la página de inicio de sesión sin mostrar el contenido del historial.
