import { Component, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { OcupacionPorSesion, SesionService } from '../../services/sesionService';

function fechaLocal(fecha: Date): string {
  const anio = fecha.getFullYear();
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  return `${anio}-${mes}-${dia}`;
}

@Component({
  selector: 'app-informe-ocupacion',
  standalone: true,
  templateUrl: './informe-ocupacion.html',
  styleUrl: './informe-ocupacion.scss',
})
export class InformeOcupacion {
  private readonly sesionesApi = inject(SesionService);
  private solicitudActual = 0;

  readonly fecha = signal(fechaLocal(new Date()));
  readonly fechaConsultada = signal<string | null>(null);
  readonly sesiones = signal<OcupacionPorSesion[]>([]);
  readonly cargando = signal(false);
  readonly error = signal('');
  readonly ocupacionMedia = computed(() => {
    const sesiones = this.sesiones();
    if (sesiones.length === 0) return 0;
    return sesiones.reduce((suma, sesion) => suma + sesion.porcentajeOcupacion, 0) / sesiones.length;
  });
  readonly fechaLegible = computed(() => {
    const fecha = this.fechaConsultada();
    if (!fecha) return '';
    const [anio, mes, dia] = fecha.split('-').map(Number);
    return new Intl.DateTimeFormat('es-ES', { day: 'numeric', month: 'long', year: 'numeric' })
      .format(new Date(anio, mes - 1, dia, 12));
  });

  constructor() { void this.cargarInforme(this.fecha()); }

  actualizarFecha(evento: Event): void {
    const nuevaFecha = (evento.target as HTMLInputElement).value;
    if (nuevaFecha === this.fecha()) return;
    this.fecha.set(nuevaFecha);
    if (!nuevaFecha) {
      this.solicitudActual++;
      this.sesiones.set([]);
      this.fechaConsultada.set(null);
      this.cargando.set(false);
      return;
    }
    void this.cargarInforme(nuevaFecha);
  }

  async cargarInforme(fechaSolicitada: string): Promise<void> {
    const solicitud = ++this.solicitudActual;
    this.cargando.set(true);
    this.error.set('');
    this.sesiones.set([]);
    this.fechaConsultada.set(null);
    try {
      const resultado = await firstValueFrom(this.sesionesApi.consultarOcupacion(fechaSolicitada));
      if (solicitud === this.solicitudActual) {
        this.sesiones.set(resultado);
        this.fechaConsultada.set(fechaSolicitada);
      }
    } catch (error) {
      if (solicitud === this.solicitudActual) this.error.set(this.mensajeDeError(error));
    } finally {
      if (solicitud === this.solicitudActual) this.cargando.set(false);
    }
  }

  horaLegible(fechaHora: string): string { return fechaHora.slice(11, 16); }

  porcentaje(valor: number): string {
    return `${new Intl.NumberFormat('es-ES', { maximumFractionDigits: 1 }).format(valor)} %`;
  }

  anchoBarra(valor: number): string { return `${Math.max(0, Math.min(100, valor))}%`; }

  nivelOcupacion(valor: number): string {
    if (valor >= 80) return 'alto';
    if (valor >= 50) return 'medio';
    return 'bajo';
  }

  private mensajeDeError(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const cuerpo = error.error as { detail?: unknown; message?: unknown; error?: unknown } | null;
      if (typeof cuerpo?.detail === 'string') return cuerpo.detail;
      if (typeof cuerpo?.message === 'string') return cuerpo.message;
      if (typeof cuerpo?.error === 'string') return cuerpo.error;
      if (error.status === 0) return 'No se pudo conectar con el servidor. Revisa tu conexión e inténtalo de nuevo.';
    }
    return 'No se pudo generar el informe. Comprueba la fecha e inténtalo de nuevo.';
  }
}
