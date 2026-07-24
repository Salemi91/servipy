# Git Workflow — ServiPy

## Ramas
- `main`: rama protegida, siempre desplegable.
- `feature/<nombre>`: nuevas funcionalidades.
- `fix/<nombre>`: correcciones de bugs.
- `chore/<nombre>`: tareas de mantenimiento, config, docs.

## Commits convencionales
Formato: `<tipo>: <descripción breve>`

Tipos permitidos:
- `feat`: nueva funcionalidad
- `fix`: corrección de bug
- `docs`: documentación
- `chore`: mantenimiento, dependencias
- `refactor`: reestructuración sin cambio funcional
- `test`: agregar o modificar tests
- `style`: formato, espacios, sin cambio lógico

## Pull Requests
- Título conciso (máx. 70 caracteres).
- Descripción: qué cambia, por qué, qué se probó.
- Merge a `main` solo tras revisión de al menos un compañero.
- Squash merge preferido para mantener historial limpio.

## Reglas
- No force-push en ramas compartidas.
- No commits directos a `main`.
- Resolver conflictos localmente antes de solicitar merge.
- Mantener ramas cortas (idealmente < 1 día de trabajo).
