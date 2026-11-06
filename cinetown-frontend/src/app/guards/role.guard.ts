import { inject } from '@angular/core';

import {
  CanActivateFn,
  Router
} from '@angular/router';

import { AuthService } from '../services/authService';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.estaAutenticado()) {
    return router.createUrlTree(
      ['/login'],
      {queryParams: {
          returnUrl: state.url
        }
      }
    );

  }

  const rolesPermitidos = route.data['roles'] as string[];

  if (authService.tieneAlgunRol(rolesPermitidos)) {
    return true;
  }
  return router.createUrlTree(['/']);
};
