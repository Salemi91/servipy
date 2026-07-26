import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';

export const PUBLIC_ROUTES: Routes = [
  { path: '', component: HomeComponent },
  {
    path: 'profesionales',
    loadChildren: () =>
      import('./catalog/catalog.routes').then((m) => m.CATALOG_ROUTES),
  },
];
