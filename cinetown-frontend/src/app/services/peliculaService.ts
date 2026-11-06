import { Injectable, inject } from '@angular/core';
import { obtienePeliculasDto } from '../models/dto/peliculaDTO';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {PeliculaDetalle} from '../models/pelicula-detalle.model';

@Injectable({
  providedIn: 'root'
})
export class PeliculaService {

  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  getPeliculas(): Observable<obtienePeliculasDto[]> {
    return this.http.get<obtienePeliculasDto[]>(`${this.apiUrl}/peliculas`);
  }
  obtenerDetalle(id: number): Observable<PeliculaDetalle> {
    return this.http.get<PeliculaDetalle>(`${this.apiUrl}/${id}`);
  }
}
