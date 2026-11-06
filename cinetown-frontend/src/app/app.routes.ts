import { Routes } from '@angular/router';
import {Landing} from './pages/landing/landing';
import {Login} from './pages/login/login';
import {RegistroComponent} from './pages/registro/registro';

export const routes: Routes = [
  {path: "", component: Landing},
  { path: 'registro', component: RegistroComponent },
  {path: 'login', component: Login},
  {path: '**', redirectTo: ''},
  {path: 'peliculas/:id', loadComponent: () =>
      import('./models/pelicula-detalle.model')
        .then(m => m.PeliculaDetalleComponent) },
];
