import { Routes } from '@angular/router';
import {Landing} from './pages/landing/landing';
import {Login} from './pages/login/login';
import {RegistroComponent} from './pages/registro/registro';

export const routes: Routes = [
  {path: '', component: Landing},
  {path: 'registro', component: RegistroComponent },
  {path: 'login', component: Login},
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
]

