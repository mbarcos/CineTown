import { Routes } from '@angular/router';
import {Landing} from './pages/landing/landing';
import {Login} from './pages/login/login';
import {RegistroComponent} from './pages/registro/registro';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  {path: '', component: Landing},
  {path: 'registro', component: RegistroComponent },
  {path: 'login', component: Login},
  {
    path: 'gestion-peliculas',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./pages/gestion-peliculas/gestion-peliculas')
      .then(m => m.GestionPeliculas),
  },
  {
    path: 'gestion-sesiones',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./pages/gestion-sesiones/gestion-sesiones')
      .then(m => m.GestionSesiones),
  },
  {
    path: 'peliculas/:id',
    loadComponent: () =>
      import('./pages/pelicula-detalle/pelicula-detalle')
        .then(m => m.PeliculaDetalle),
  },
  {
    path: 'peliculas/:id/sesiones',
    loadComponent: () =>
      import('./pages/sesiones-pelicula/sesiones-pelicula')
        .then(m => m.SesionesPelicula),
  },
  {
    path: 'sesiones/:id',
    loadComponent: () =>
      import('./pages/compra/compra')
        .then(m => m.Compra),
  },
]

