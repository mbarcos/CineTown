import { Injectable, inject } from '@angular/core';
import { obtienePeliculasDto } from '../models/dto/peliculaDTO';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {PeliculaResponse} from '../models/pelicula-detalle.model';

export interface PaginaPeliculas {
  content: PeliculaResponse[];
  totalElements: number;
  totalPages: number;
}

export interface PeliculaRequest {
  titulo: string;
  sinopsis: string;
  duracion: number;
  anio: number;
  clasificacion: PeliculaResponse['clasificacion'];
  activo: boolean;
  generos: string[];
}

export type BusquedaCartel = Pick<PeliculaRequest, 'titulo' | 'anio' | 'sinopsis'>;

export interface RecaudacionPelicula {
  peliculaId: number;
  titulo: string;
  entradasVendidas: number;
  recaudacionTotal: number;
}

@Injectable({
  providedIn: 'root'
})
export class PeliculaService {

  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  getPeliculas(): Observable<obtienePeliculasDto[]> {
    return this.http.get<obtienePeliculasDto[]>(`${this.apiUrl}/peliculas`);
  }
  getPeliculasParaGestion(): Observable<PeliculaResponse[]> {
    return this.http.get<PeliculaResponse[]>(`${this.apiUrl}/peliculas/gestion`);
  }
  consultarPagina(params: Record<string, string | number | boolean>): Observable<PaginaPeliculas> {
    return this.http.get<PaginaPeliculas>(`${this.apiUrl}/peliculas/gestion`, { params });
  }
  previsualizarCartel(datos: BusquedaCartel): Observable<{ cartelUrl: string | null }> {
    return this.http.post<{ cartelUrl: string | null }>(`${this.apiUrl}/peliculas/gestion/cartel`, datos);
  }
  getGeneros(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/generos/catalogo`);
  }
  crearPelicula(pelicula: PeliculaRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/peliculas`, pelicula);
  }
  actualizarPelicula(id: number, pelicula: PeliculaRequest): Observable<PeliculaResponse> {
    return this.http.put<PeliculaResponse>(`${this.apiUrl}/peliculas/${id}`, pelicula);
  }
  cambiarEstadoCartelera(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/peliculas/${id}/cartelera`, {});
  }
  obtenerDetalle(id: number): Observable<PeliculaResponse> {
    return this.http.get<PeliculaResponse>(`${this.apiUrl}/peliculas/${id}`);
  }

  obtenerRecaudacion(id: number): Observable<RecaudacionPelicula> {
    return this.http.get<RecaudacionPelicula>(`${this.apiUrl}/peliculas/${id}/recaudacion`);
  }
}
