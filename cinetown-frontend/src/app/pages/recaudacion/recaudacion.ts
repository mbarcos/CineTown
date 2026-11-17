import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { PeliculaResponse } from '../../models/pelicula-detalle.model';
import { PeliculaService, RecaudacionPelicula } from '../../services/peliculaService';

@Component({
  selector: 'app-recaudacion',
  standalone: true,
  templateUrl: './recaudacion.html',
  styleUrl: './recaudacion.scss',
})
export class Recaudacion {
  private readonly peliculasApi = inject(PeliculaService);
  private solicitudActual = 0;

  readonly peliculas = signal<PeliculaResponse[]>([]);
  readonly peliculaId = signal<number | null>(null);
  readonly recaudacion = signal<RecaudacionPelicula | null>(null);
  readonly cargandoPeliculas = signal(true);
  readonly cargandoRecaudacion = signal(false);
  readonly error = signal('');

  constructor() { void this.cargarPeliculas(); }

  async cargarPeliculas(): Promise<void> {
    this.cargandoPeliculas.set(true);
    this.error.set('');
    try {
      const peliculas = await firstValueFrom(this.peliculasApi.getPeliculasParaGestion());
      this.peliculas.set(peliculas);
      const primeraPelicula = peliculas[0];
      if (primeraPelicula) {
        this.peliculaId.set(primeraPelicula.id);
        await this.cargarRecaudacion(primeraPelicula.id);
      }
    } catch (error) {
      this.error.set(this.mensajeDeError(error, 'No se pudieron cargar las películas.'));
    } finally {
      this.cargandoPeliculas.set(false);
    }
  }

  async seleccionarPelicula(evento: Event): Promise<void> {
    const valor = (evento.target as HTMLSelectElement).value;
    const id = Number(valor);
    if (!Number.isInteger(id) || id < 1) {
      this.peliculaId.set(null);
      this.recaudacion.set(null);
      return;
    }
    this.peliculaId.set(id);
    await this.cargarRecaudacion(id);
  }

  async cargarRecaudacion(id = this.peliculaId()): Promise<void> {
    if (id === null) return;
    const solicitud = ++this.solicitudActual;
    this.cargandoRecaudacion.set(true);
    this.recaudacion.set(null);
    this.error.set('');
    try {
      const resultado = await firstValueFrom(this.peliculasApi.obtenerRecaudacion(id));
      if (solicitud === this.solicitudActual) this.recaudacion.set(resultado);
    } catch (error) {
      if (solicitud === this.solicitudActual) {
        this.error.set(this.mensajeDeError(error, 'No se pudo consultar la recaudación de esta película.'));
      }
    } finally {
      if (solicitud === this.solicitudActual) this.cargandoRecaudacion.set(false);
    }
  }

  dinero(valor: number): string {
    return valor.toLocaleString('es-ES', { style: 'currency', currency: 'EUR' });
  }

  private mensajeDeError(error: unknown, alternativa: string): string {
    if (error instanceof HttpErrorResponse) {
      const cuerpo = error.error as { detail?: unknown; message?: unknown; error?: unknown } | null;
      if (typeof cuerpo?.detail === 'string') return cuerpo.detail;
      if (typeof cuerpo?.message === 'string') return cuerpo.message;
      if (typeof cuerpo?.error === 'string') return cuerpo.error;
      if (error.status === 0) return 'No se pudo conectar con el servidor. Revisa tu conexión e inténtalo de nuevo.';
    }
    return alternativa;
  }
}
