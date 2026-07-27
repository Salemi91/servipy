import { Routes } from '@angular/router';
import { profileCompleteGuard } from './guards/profile-complete.guard';

export const PROFESSIONAL_ROUTES: Routes = [
  {
    path: 'onboarding',
    loadComponent: () =>
      import('./onboarding/onboarding-wizard.component').then(
        (m) => m.OnboardingWizardComponent
      ),
  },
  {
    path: 'requests',
    canActivate: [profileCompleteGuard],
    loadChildren: () =>
      import('./requests/requests.routes').then((m) => m.REQUESTS_ROUTES),
  },
  { path: '', redirectTo: 'requests', pathMatch: 'full' },
];
