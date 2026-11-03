import { Injectable, inject } from '@angular/core';
import { obtienePeliculasDto } from '../models/dto/peliculaDTO';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PeliculaService {

  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  getPeliculas(): Observable<obtienePeliculasDto[]> {
    return this.http.get<obtienePeliculasDto[]>(`${this.apiUrl}/peliculas`);
  }
}
