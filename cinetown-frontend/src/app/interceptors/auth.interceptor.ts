import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { AuthService } from '../services/authService';
import { environment } from '../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const authService = inject(AuthService);

  const token = authService.obtenerToken();

  const apiUrl = environment.apiUrl.replace(/\/$/, '');

  if (!req.url.startsWith(`${apiUrl}/`)) {
    return next(req);
  }

  const path = req.url.substring(apiUrl.length).split('?')[0];

  const esAutenticacion = path.startsWith('/auth/');

  const esLecturaPublica =
    req.method === 'GET' && (
      path === '/peliculas' ||
      /^\/peliculas\/[^/]+$/.test(path) ||
      path === '/generos' ||
      path === '/sesiones' ||
      /^\/sesiones\/[^/]+$/.test(path)
    );

  if (!token || esAutenticacion || esLecturaPublica) {
    return next(req);
  }

  const requestConToken = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  return next(requestConToken);
};
