# Seguridad — ServiPy

## Secretos
- **Prohibido** incluir contraseñas, tokens, claves o secrets en el repositorio.
- Usar variables de entorno o archivos locales ignorados por git (`application-local.yml`, `.env`).
- El `.gitignore` debe incluir: `*.env`, `application-local.yml`, `*.key`, `*.pem`.

## Autenticación (JWT)
- Access token: vida corta (15-30 min).
- Refresh token: vida larga (7 días), almacenado en httpOnly cookie o storage seguro.
- Endpoints públicos: login, registro, refresh.
- Todos los demás endpoints requieren token válido.

## Autorización
- Roles: `ROLE_CLIENT`, `ROLE_PROFESSIONAL`, `ROLE_ADMIN`.
- Protección a nivel de endpoint con `@PreAuthorize` o configuración en SecurityFilterChain.
- ADMIN hereda acceso a todo; CLIENT y PROFESSIONAL solo a sus recursos.

## CORS
- Permitir solo el origen del frontend (configurable por entorno).
- Métodos: GET, POST, PUT, DELETE, OPTIONS.
- Headers: Authorization, Content-Type.

## Buenas prácticas
- Validar inputs en backend (Bean Validation).
- Sanitizar inputs en frontend antes de enviar.
- No loggear tokens ni datos sensibles.
- HTTPS obligatorio en producción.
