# ServiPy Frontend

Aplicación web Angular para la plataforma ServiPy.

## Requisitos

- Node.js 20+ (recomendado 22 LTS)
- npm 10+

## Instalación

```bash
cd frontend
npm install
```

## Desarrollo

```bash
npm start
# o
ng serve
```

El servidor arranca en `http://localhost:4200`.

## Build

```bash
ng build
```

El output se genera en `dist/servipy-frontend/`.

## Tests

```bash
ng test --watch=false --browsers=ChromeHeadless
```

## Estructura

```
src/app/
├── core/              # Servicios singleton, guards, interceptors, layout
│   ├── config/        # Tokens de inyección (API base URL, etc.)
│   ├── http/          # ApiService, interceptors HTTP
│   ├── guards/        # Route guards (auth, roles)
│   └── layout/        # Shell principal (header, footer, router-outlet)
├── shared/            # Componentes reutilizables, modelos, utilidades
│   ├── components/    # Componentes compartidos
│   ├── models/        # Interfaces TypeScript (ErrorResponse, etc.)
│   └── utilities/     # Helpers y funciones utilitarias
└── features/          # Vertical slices por funcionalidad
    ├── public/        # Páginas públicas (home)
    ├── authentication/# Login, registro
    ├── client/        # Dashboard y flujos del cliente
    ├── professional/  # Dashboard y flujos del profesional
    └── administration/# Panel de administración
```

## Convenciones

- Componentes standalone (sin NgModules).
- Lazy loading por feature vía `loadChildren`.
- URL base de la API centralizada en `environment.ts` → `ApiService`.
- No escribir URLs del backend directamente en componentes.
- Mobile-first con Tailwind CSS.
- TypeScript estricto habilitado.
