import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, catchError, of } from 'rxjs';
import { ProfessionalProfileApiService } from '../services/professional-profile.service';

/**
 * Guard that checks if the professional has a completed profile.
 * If not (404), redirects to onboarding.
 */
export const profileCompleteGuard: CanActivateFn = () => {
  const profileApi = inject(ProfessionalProfileApiService);
  const router = inject(Router);

  return profileApi.getMyProfile().pipe(
    map(() => true),
    catchError(() => {
      router.navigate(['/professional/onboarding']);
      return of(false);
    })
  );
};
