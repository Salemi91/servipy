# Primer prompt para Kiro

Usar el flujo **Spec** con enfoque **Requirements-First**.

Copiar este texto en Kiro:

---

Estamos iniciando ServiPy, una aplicación web para conectar clientes con profesionales de servicios en Paraguay.

Antes de escribir código:

1. Lee completamente:
   - `README.md`
   - `docs/MVP.md`
   - `docs/API_CONTRACT.md`
   - `docs/DOMAIN_MODEL.md`
   - todos los archivos de `.kiro/steering/`

2. Analiza el alcance y señala:
   - contradicciones;
   - requisitos ambiguos;
   - riesgos de seguridad;
   - dependencias entre frontend y backend;
   - cualquier elemento que exceda el MVP.

3. Crea una Feature Spec llamada `project-foundation-and-authentication`, con enfoque Requirements-First.

La spec debe cubrir únicamente:

- estructura monorepo con `frontend/` y `backend/`;
- Angular + Tailwind;
- Spring Boot + MySQL;
- configuración local mediante variables de entorno;
- endpoint `/api/v1/health`;
- registro de cliente y profesional;
- inicio de sesión;
- JWT;
- roles `CLIENT`, `PROFESSIONAL` y `ADMIN`;
- rutas frontend protegidas por rol;
- usuario administrador cargado mediante seed;
- pruebas mínimas de autenticación y autorización.

No implementes todavía perfiles, búsquedas, servicios ofrecidos, solicitudes, mapas, WhatsApp ni dashboard.

Antes de generar código, presenta `requirements.md`, `design.md` y `tasks.md` para revisión humana. Las tareas deben ser pequeñas, verificables y asignables entre integrantes. No inventes funcionalidades fuera de los documentos.

---
