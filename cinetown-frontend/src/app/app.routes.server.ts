import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'compras',
    renderMode: RenderMode.Client,
  },
  {
    path: 'gestion-sesiones',
    renderMode: RenderMode.Client,
  },
  {
    path: 'sesiones/:id',
    renderMode: RenderMode.Server,
  },
  {
    path: 'peliculas/:id/sesiones',
    renderMode: RenderMode.Server,
  },
  {
    path: 'peliculas/:id',
    renderMode: RenderMode.Server,
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
