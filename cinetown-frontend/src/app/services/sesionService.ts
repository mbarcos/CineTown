import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { SesionConAsientos, SesionGestion } from '../models/sesion.model';
import { Sala } from '../models/sala.model';

export interface SesionRequest {
  peliculaId: number;
  salaId: number;
  fechaHoraInicio: string;
  precioBase: number;
}

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

  consultarSesionesParaGestion(): Observable<SesionGestion[]> {
    return this.http.get<SesionGestion[]>(`${this.apiUrl}/sesiones/gestion`);
  }

  consultarSalas(): Observable<Sala[]> {
    return this.http.get<Sala[]>(`${this.apiUrl}/salas`);
  }

  programarSesion(sesion: SesionRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/sesiones`, sesion);
  }

  modificarSesion(id: number, sesion: SesionRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/sesiones/${id}`, sesion);
  }
}
