# Alcance MVP de ServiPy

## Objetivo

Entregar antes del 27 de julio de 2026 una aplicación web pública y funcional que demuestre el flujo completo entre cliente, profesional y administrador.

## País y moneda de la demostración

Aunque el modelo puede admitir expansión futura, el MVP opera únicamente con:

- País: Paraguay
- Moneda: PYG
- Ciudades iniciales: Asunción, San Lorenzo, Luque, Capiatá y Fernando de la Mora

## Perfiles

### Cliente

Puede:

1. Registrarse e iniciar sesión.
2. Consultar categorías.
3. Buscar profesionales por categoría y ciudad.
4. Consultar el perfil y servicios ofrecidos.
5. Crear una solicitud.
6. Consultar el estado de sus solicitudes.
7. Abrir WhatsApp cuando una solicitud haya sido aceptada.

### Profesional

Puede:

1. Registrarse e iniciar sesión.
2. Completar y editar su perfil.
3. Permanecer como `PENDING` hasta la aprobación.
4. Crear, listar y eliminar servicios ofrecidos.
5. Cambiar su disponibilidad.
6. Consultar solicitudes recibidas.
7. Aceptar una solicitud.
8. Marcarla como finalizada.

### Administrador

Puede:

1. Iniciar sesión.
2. Consultar profesionales pendientes.
3. Aprobar o rechazar perfiles profesionales.
4. Crear y listar categorías.

## Estados

### Aprobación profesional

- `PENDING`
- `APPROVED`
- `REJECTED`

### Disponibilidad profesional

- `AVAILABLE_TODAY`
- `WEEKENDS`
- `BUSY`

### Solicitud

- `PENDING`
- `ACCEPTED`
- `COMPLETED`
- `CANCELLED`

## Fuera del MVP

No implementar durante la hackathon:

- pagos;
- chat interno;
- recuperación de contraseña;
- inicio de sesión social;
- doble factor;
- confirmación por correo;
- reseñas y calificaciones;
- dashboard avanzado;
- notificaciones;
- presupuestos negociables;
- múltiples monedas operativas;
- mapa avanzado o geolocalización en tiempo real;
- verificación de identidad;
- aplicación móvil nativa.

## Criterio de éxito

El MVP es exitoso cuando:

1. Los tres perfiles pueden iniciar sesión y acceder solo a sus funciones.
2. Un administrador aprueba un profesional.
3. Un cliente encuentra al profesional y crea una solicitud.
4. El profesional acepta y finaliza la solicitud.
5. El cliente ve el cambio de estado y puede abrir WhatsApp.
6. La demo funciona desde una URL pública.
