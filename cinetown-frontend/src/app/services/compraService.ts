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

export interface CompraResumen {
  codCompra: string;
  titulo: string;
  numeroSala: number;
  fechaHoraInicio: string;
  numEntradas: number;
  precioTotal: number;
  estado: boolean;
  fechaCompra: string;
}

export interface PaginaCompras {
  content: CompraResumen[];
  number: number;
  totalPages: number;
  totalElements: number;
  first: boolean;
  last: boolean;
}

@Injectable({ providedIn: 'root' })
export class CompraService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  comprarEntradas(datos: CompraRequest): Observable<CompraResponse> {
    return this.http.post<CompraResponse>(`${this.apiUrl}/compras`, datos);
  }

  consultarCompras(pagina = 0, tamano = 10): Observable<PaginaCompras> {
    return this.http.get<PaginaCompras>(`${this.apiUrl}/compras`, {
      params: { page: pagina, size: tamano },
    });
  }

  cancelarCompra(codigo: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/compras/${encodeURIComponent(codigo)}/cancelar`, {});
  }
}
