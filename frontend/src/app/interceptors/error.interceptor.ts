import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        authService.clearToken();
        void router.navigate(['/login']);
      } else if (error.status === 403) {
        void router.navigate(['/acesso-negado']);
      }

      const message =
        error.error?.message ||
        error.message ||
        'Erro inesperado ao comunicar com o servidor.';

      return throwError(() => new Error(message));
    })
  );
};
