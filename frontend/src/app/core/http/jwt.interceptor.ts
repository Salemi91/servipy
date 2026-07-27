import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

/**
 * Interceptor funcional que adjunta el token JWT a las peticiones
 * y maneja respuestas 401 con código TOKEN_EXPIRED ejecutando logout.
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // Adjuntar token si existe
  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Solo hacer logout si es un 401 con código TOKEN_EXPIRED del backend
      // y no es un endpoint de auth (evitar loop)
      if (
        error.status === 401 &&
        !req.url.includes('/auth/login') &&
        !req.url.includes('/auth/register') &&
        error.error?.code === 'TOKEN_EXPIRED'
      ) {
        authService.logout();
      }
      return throwError(() => error);
    })
  );
};
