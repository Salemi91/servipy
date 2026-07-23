# Estrategia de ramas

## Rama protegida

`main` representa el estado integrable y demostrable.

## Ramas permitidas

```text
feature/<descripcion-corta>
fix/<descripcion-corta>
docs/<descripcion-corta>
chore/<descripcion-corta>
```

Ejemplos:

```text
feature/authentication
feature/professional-profile
feature/service-search
feature/service-request
feature/admin-approval
chore/aws-deployment
fix/jwt-expiration
```

## Reglas

1. No hacer push directo a `main`.
2. Todo cambio entra mediante Pull Request.
3. Cada PR debe resolver una tarea concreta.
4. Un integrante distinto revisa el PR.
5. La rama debe actualizarse con `main` antes del merge.
6. Usar squash merge para mantener un historial claro.
7. Eliminar la rama después del merge.
8. Evitar ramas que duren más de un día.
9. No combinar refactors grandes con una funcionalidad.
10. No cambiar el contrato API sin informar al equipo.

## Commits

Usar Conventional Commits:

```text
feat: add professional registration
fix: validate expired jwt
test: add service request tests
docs: document local setup
chore: configure nginx
refactor: extract authentication service
```

## Flujo

```bash
git checkout main
git pull origin main
git checkout -b feature/nombre

# trabajar y guardar
git add .
git commit -m "feat: descripcion"
git push -u origin feature/nombre

# abrir Pull Request y solicitar revisión
```
