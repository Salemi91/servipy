import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

/**
 * Guard que protege rutas por rol.
 * Lee los roles permitidos de route.data['roles'] (string[]).
 * Redirige a / si el rol del usuario no está en la lista.
 */
export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const allowedRoles = route.data['roles'] as string[] | undefined;
  const userRole = authService.getUserRole();

  if (allowedRoles && userRole && allowedRoles.includes(userRole)) {
    return true;
  }

  router.navigate(['/']);
  return false;
};
