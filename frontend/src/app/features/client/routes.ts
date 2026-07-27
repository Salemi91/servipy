import { Routes } from '@angular/router';
import { authGuard } from '../../core/guards/auth.guard';
import { ClientLayoutComponent } from './client-layout.component';
import { ProfilePageComponent } from './profile/profile-page.component';
import { RequestHistoryPageComponent } from './requests/request-history-page.component';

export const CLIENT_ROUTES: Routes = [
  {
    path: '',
    component: ClientLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'profile', component: ProfilePageComponent },
      { path: 'requests', component: RequestHistoryPageComponent },
      { path: '', redirectTo: 'profile', pathMatch: 'full' },
    ],
  },
];
