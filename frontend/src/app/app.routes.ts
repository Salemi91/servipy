import { Routes } from '@angular/router';
import { LayoutComponent } from './core/layout/layout.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

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
        path: 'register',
        loadComponent: () =>
          import('./features/authentication/register/register.component').then((m) => m.RegisterComponent),
      },
      {
        path: 'client',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['CLIENT'] },
        loadChildren: () =>
          import('./features/client/routes').then((m) => m.CLIENT_ROUTES),
      },
      {
        path: 'professional',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['PROFESSIONAL'] },
        loadChildren: () =>
          import('./features/professional/routes').then((m) => m.PROFESSIONAL_ROUTES),
      },
      {
        path: 'admin',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['ADMIN'] },
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
