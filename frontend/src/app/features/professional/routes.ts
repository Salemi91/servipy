import { Routes } from '@angular/router';

export const PROFESSIONAL_ROUTES: Routes = [
  {
    path: 'requests/:professionalId',
    loadChildren: () =>
      import('./requests/requests.routes').then((m) => m.REQUESTS_ROUTES),
  },
  { path: '', redirectTo: 'requests/1', pathMatch: 'full' }, // demo redirect for MVP
];
