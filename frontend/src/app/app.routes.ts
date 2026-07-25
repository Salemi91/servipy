import { Routes } from '@angular/router';
import { LayoutComponent } from './core/layout/layout.component';

export const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      {
        path: '',
        loadChildren: () =>
          import('./features/public/routes').then((m) => m.PUBLIC_ROUTES),
      },
      {
        path: 'login',
        loadChildren: () =>
          import('./features/authentication/routes').then((m) => m.AUTH_ROUTES),
      },
      {
        path: 'client',
        loadChildren: () =>
          import('./features/client/routes').then((m) => m.CLIENT_ROUTES),
      },
      {
        path: 'professional',
        loadChildren: () =>
          import('./features/professional/routes').then((m) => m.PROFESSIONAL_ROUTES),
      },
      {
        path: 'admin',
        loadChildren: () =>
          import('./features/administration/routes').then((m) => m.ADMIN_ROUTES),
      },
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
