import { Routes } from '@angular/router';
import { authGuard } from '../../core/guards/auth.guard';
import { ClientLayoutComponent } from './client-layout.component';
import { ProfilePageComponent } from './profile/profile-page.component';
import { RequestHistoryPageComponent } from './requests/request-history-page.component';
import { RequestDetailPageComponent } from './requests/request-detail-page.component';

export const CLIENT_ROUTES: Routes = [
  {
    path: '',
    component: ClientLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'profile', component: ProfilePageComponent },
      { path: 'requests', component: RequestHistoryPageComponent },
      { path: 'requests/:id', component: RequestDetailPageComponent },
      { path: '', redirectTo: 'profile', pathMatch: 'full' },
    ],
  },
];
