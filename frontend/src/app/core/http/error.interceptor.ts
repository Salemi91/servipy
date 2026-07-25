import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

/**
 * Interceptor global de errores HTTP.
 * FOUNDATION: solo loggea errores por ahora.
 * Cuando se implemente JWT, aquí se manejará el 401 (logout + redirect).
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Foundation: log básico. No implementa logout ni redirect todavía.
      console.error(`[HTTP Error] ${error.status} - ${error.url}`, error.message);
      return throwError(() => error);
    })
  );
};
