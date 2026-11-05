import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { environment } from '../../environments/environment';

import { LoginRequestDTO } from '../models/dto/loginRequestDTO';
import { AuthResponseDTO } from '../models/dto/authResponseDTO';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http = inject(HttpClient);
  private router = inject(Router);

  private apiUrl = environment.apiUrl;

  private readonly storageKey = 'cinetown_auth';

  private sesion = signal<AuthResponseDTO | null>(
    this.leerSesion()
  );

  readonly usuarioActual = this.sesion.asReadonly();

  readonly estaAutenticado = computed(
    () => !!this.sesion()?.token
  );

  readonly roles = computed(
    () => this.sesion()?.roles ?? []
  );

  login(datos: LoginRequestDTO): Observable<AuthResponseDTO> {
    return this.http.post<AuthResponseDTO>(
      `${this.apiUrl}/auth/login`,
      datos
    ).pipe(
      tap(respuesta => {
        this.guardarSesion(respuesta);
      })
    );
  }

  private guardarSesion(respuesta: AuthResponseDTO): void {
    sessionStorage.setItem(
      this.storageKey,
      JSON.stringify(respuesta)
    );
    this.sesion.set(respuesta);
  }

  private leerSesion(): AuthResponseDTO | null {
    try {
      const sesionGuardada = sessionStorage.getItem(
        this.storageKey
      );

      if (!sesionGuardada) {
        return null;
      }

      const sesion = JSON.parse(sesionGuardada) as AuthResponseDTO;

      if (
        !sesion.token ||
        !sesion.nombreUsuario ||
        !Array.isArray(sesion.roles)
      ) {
        return null;
      }

      return sesion;

    } catch {
      return null;
    }
  }

  obtenerToken(): string | null {return this.sesion()?.token ?? null;}

  tieneAlgunRol(roles: string[]): boolean {
    return roles.some(rol => this.roles().includes(rol));
  }

  cerrarSesion(): void {
    sessionStorage.removeItem(this.storageKey);
    this.sesion.set(null);
    this.router.navigate(['/']);
  }

}
