import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { PerfilAcesso } from '../models/auth.model';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  return router.createUrlTree(['/login']);
};

export function roleGuard(allowedRoles: PerfilAcesso[]): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      return router.createUrlTree(['/login']);
    }

    const perfil = authService.perfil();
    if (perfil === 'ADMIN' || allowedRoles.includes(perfil)) {
      return true;
    }

    return router.createUrlTree(['/acesso-negado']);
  };
}
