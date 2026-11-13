import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { SesionConAsientos } from '../models/sesion.model';

@Injectable({
  providedIn: 'root',
})
export class SesionService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  consultarSesionesActivasPorPelicula(peliculaId: number): Observable<SesionConAsientos[]> {
    return this.http.get<SesionConAsientos[]>(`${this.apiUrl}/sesiones`, {
      params: { fecha: '', peliculaId },
    });
  }

  consultarSesionesActivasPorPeliculaYFecha(
    peliculaId: number,
    fecha: string,
  ): Observable<SesionConAsientos[]> {
    return this.http.get<SesionConAsientos[]>(`${this.apiUrl}/sesiones`, {
      params: { fecha, peliculaId },
    });
  }

  consultarSesionPorId(id: number): Observable<SesionConAsientos> {
    return this.http.get<SesionConAsientos>(`${this.apiUrl}/sesiones/${id}`);
  }
}
