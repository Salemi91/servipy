# Testing — ServiPy

## Backend
- Framework: JUnit 5 + Mockito.
- Tests unitarios obligatorios para la capa de servicio.
- Tests de integración para endpoints críticos (auth, booking).
- Nombrado: `should_<resultado>_when_<condición>`.
- Patrón AAA: Arrange, Act, Assert.

## Frontend
- Framework: Jasmine + Karma (default Angular) o Jest si se configura.
- Tests unitarios en componentes con lógica significativa.
- Servicios HTTP: mockear HttpClient con `HttpClientTestingModule`.
- Nombrado: `it('should <comportamiento esperado>')`.

## Estrategia hackathon
- Cobertura mínima no bloquea CI, pero se busca cubrir happy paths.
- Priorizar tests en: autenticación, creación de bookings, registro de usuarios.
- No se requieren tests E2E para el MVP.

## Ejecución
- Backend: `./mvnw test` desde `/backend`.
- Frontend: `ng test --watch=false` desde `/frontend`.
