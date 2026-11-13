import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CompraRequest {
  sesionId: number;
  numEntradas: number;
  email: string;
}

export interface CompraResponse {
  codCompra: string;
  numEntradas: number;
  precioTotal: number;
}

@Injectable({ providedIn: 'root' })
export class CompraService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  comprarEntradas(datos: CompraRequest): Observable<CompraResponse> {
    return this.http.post<CompraResponse>(`${this.apiUrl}/compras`, datos);
  }
}
