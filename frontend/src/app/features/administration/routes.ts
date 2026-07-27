import { Routes } from '@angular/router';
import { AdminDashboardComponent } from './admin-dashboard.component';
import { CategoryListComponent } from './categories/category-list.component';
import { ProfessionalListComponent } from './professionals/professional-list.component';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    component: AdminDashboardComponent,
    children: [
      { path: '', redirectTo: 'categories', pathMatch: 'full' },
      { path: 'categories', component: CategoryListComponent },
      { path: 'professionals', component: ProfessionalListComponent },
    ],
  },
];
