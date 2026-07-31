# ServiPy Frontend

Aplicación web Angular para la plataforma ServiPy.

## Stack y requisitos

- **Angular 19** con componentes standalone (sin NgModules)
- **Tailwind CSS 3** (mobile-first)
- **Signals** de Angular para estado local
- **TypeScript 5.7** en modo `strict` (`strictTemplates`, `noUnusedLocals`, `noImplicitReturns`)
- **Jasmine + Karma** para tests
- Node.js 20+ (recomendado 22 LTS) y npm 10+

## Estructura de carpetas

```
src/app/
├── core/                  # Singletons: se instancian una vez en toda la app
│   ├── auth/               # AuthService: sesión, token, rol del usuario
│   ├── config/             # Tokens de inyección (API_BASE_URL, etc.)
│   ├── http/                # ApiService y servicios HTTP compartidos entre features
│   ├── guards/              # authGuard, roleGuard
│   ├── services/            # Catálogos de referencia compartidos (categorías, ciudades)
│   └── layout/              # Shell principal (header, footer, router-outlet)
├── shared/                 # Reutilizable entre features
│   ├── components/
│   ├── models/              # Interfaces TypeScript de la API
│   └── utilities/
└── features/               # Vertical slices por funcionalidad, con lazy loading
    ├── public/              # Home, catálogo público
    ├── authentication/      # Login, registro
    ├── client/              # Perfil e historial del cliente
    ├── professional/        # Onboarding y gestión de solicitudes
    └── administration/      # Panel de administración
```

Alias configurados en `tsconfig.json`: `@core/*`, `@shared/*`, `@features/*`, `@env`. Se usan en lugar de rutas relativas profundas (`../../../../`) al importar entre carpetas de nivel superior.

## Manejo de estado y HTTP

- **Estado local**: `signal()` para valores reactivos de componente; sin librerías de estado global, el MVP no las necesita.
- **HTTP centralizado**: todo acceso a la API pasa por `ApiService` (`core/http/api.service.ts`), que resuelve la URL base desde el token `API_BASE_URL`. Ningún componente inyecta `HttpClient` directamente ni escribe URLs del backend.
- **Interceptores funcionales**:
  - `jwtInterceptor` adjunta `Authorization: Bearer <token>` a cada request autenticado.
- **Guards funcionales**: `authGuard` protege rutas que requieren sesión; `roleGuard` restringe por rol usando `data: { roles: [...] }` en la definición de ruta.
- **Servicios por feature**: cada feature con necesidades HTTP propias tiene su servicio (`client-profile.service.ts`, `professional-profile.service.ts`, etc.), que usa `ApiService` internamente. Los servicios compartidos entre features (como el de solicitudes de servicio) viven en `core/http`.

## Comandos de desarrollo

```bash
# Instalación
npm install

# Servidor de desarrollo (http://localhost:4200)
npm start
# equivalente a: ng serve

# Build de producción
ng build --configuration production
# output en dist/servipy-frontend/

# Tests unitarios
npm test
# equivalente a: ng test --watch=false --browsers=ChromeHeadless

# Linter
npm run lint
```

## Configuración de entorno

La URL base de la API se define por entorno:

| Archivo | Uso | Valor |
|---------|-----|-------|
| `src/environments/environment.ts` | Desarrollo (`ng serve`) | `http://localhost:8080/api/v1` |
| `src/environments/environment.prod.ts` | Build de producción | `/api/v1` (ruta relativa, resuelta por el proxy de Nginx) |

`angular.json` sustituye `environment.ts` por `environment.prod.ts` automáticamente al compilar con `--configuration production` (`fileReplacements`). No hace falta cambiar nada a mano entre entornos.

## Convenciones

- Componentes standalone, `inject()` en lugar de constructor injection.
- Lazy loading por feature vía `loadChildren`.
- Sin `any`: toda respuesta de la API tiene su interfaz en `shared/models/` o en el feature correspondiente.
- Formularios con Reactive Forms y validación en tiempo real.
- Accesibilidad: `label` asociado a cada input, `role="alert"` en mensajes de error, `alt` en imágenes, foco visible (`focus:ring-2`).

Ver [`PROJECT.md` § 2.3](../PROJECT.md#23-frontend) para el detalle completo.
