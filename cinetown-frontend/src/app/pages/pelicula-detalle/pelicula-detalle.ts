import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { map, switchMap } from 'rxjs';
import { Clasificacion } from '../../models/pelicula-detalle.model';
import { PeliculaService } from '../../services/peliculaService';
import { SesionService } from '../../services/sesionService';
import { SesionConAsientos } from '../../models/sesion.model';
import { obtenerFondoPoster } from '../../shared/utils/pelicula-poster';

interface SesionPresentacion extends SesionConAsientos {
  fechaLegible: string;
  horaLegible: string;
  precioLegible: string;
}

@Component({
  selector: 'app-pelicula-detalle',
  standalone: true,
  templateUrl: './pelicula-detalle.html',
  styleUrl: './pelicula-detalle.scss',
})
export class PeliculaDetalle {
  private readonly etiquetas: Record<Clasificacion, string> = {
    TP: 'Todos los públicos',
    P7: '+7',
    P12: '+12',
    P16: '+16',
    P18: '+18',
  };

  private readonly route = inject(ActivatedRoute);
  private readonly peliculaService = inject(PeliculaService);
  private readonly sesionService = inject(SesionService);

  private readonly fechaHoraActualMadrid = new Intl.DateTimeFormat('en-GB', {
    timeZone: 'Europe/Madrid',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hourCycle: 'h23',
  });
  private readonly fechaSesionFormatter = new Intl.DateTimeFormat('es-ES', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    timeZone: 'UTC',
  });
  private readonly horaSesionFormatter = new Intl.DateTimeFormat('es-ES', {
    hour: '2-digit',
    minute: '2-digit',
    hourCycle: 'h23',
    timeZone: 'UTC',
  });
  private readonly precioFormatter = new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
  });

  readonly pelicula = toSignal(
    this.route.paramMap.pipe(
      map((params) => Number(params.get('id'))),
      switchMap((id) => this.peliculaService.obtenerDetalle(id)),
    ),
  );

  readonly sesiones = toSignal(
    this.route.paramMap.pipe(
      map((params) => Number(params.get('id'))),
      switchMap((id) => this.sesionService.consultarSesionesActivasPorPelicula(id)),
    ),
  );

  readonly sesionesProximas = computed<SesionPresentacion[]>(() => {
    const ahora = this.obtenerAhoraLocalMadrid();
    const sesiones = this.sesiones() ?? [];

    return sesiones
      .map((sesion) => ({ sesion, instanteLocal: this.convertirFechaLocal(sesion.fechaHoraInicio) }))
      .filter(({ instanteLocal }) => Number.isFinite(instanteLocal) && instanteLocal > ahora)
      .sort((a, b) => a.instanteLocal - b.instanteLocal)
      .slice(0, 6)
      .map(({ sesion, instanteLocal }) => {
        const fecha = new Date(instanteLocal);
        const fechaLegible = this.fechaSesionFormatter.format(fecha);

        return {
          ...sesion,
          fechaLegible: fechaLegible.charAt(0).toLocaleUpperCase('es-ES') + fechaLegible.slice(1),
          horaLegible: this.horaSesionFormatter.format(fecha),
          precioLegible: this.precioFormatter.format(sesion.precioBase),
        };
      });
  });

  readonly sesionSeleccionadaId = signal<number | null>(null);

  readonly precioDesde = computed(() => {
    const precios = this.sesionesProximas().map((sesion) => sesion.precioBase);
    return precios.length ? this.precioFormatter.format(Math.min(...precios)) : null;
  });

  etiquetaClasificacion(clasificacion: Clasificacion): string {
    return this.etiquetas[clasificacion];
  }

  seleccionarSesion(sesionId: number): void {
    this.sesionSeleccionadaId.set(sesionId);
  }

  duracionTexto(minutos: number): string {
    const horas = Math.floor(minutos / 60);
    const minutosRestantes = minutos % 60;

    if (!horas) return `${minutosRestantes} min`;
    if (!minutosRestantes) return `${horas} h`;
    return `${horas} h ${minutosRestantes} min`;
  }

  fondoPoster(id: number): string {
    return obtenerFondoPoster(id);
  }

  private obtenerAhoraLocalMadrid(): number {
    const partes = new Map(
      this.fechaHoraActualMadrid
        .formatToParts(new Date())
        .filter((parte) => parte.type !== 'literal')
        .map((parte) => [parte.type, Number(parte.value)]),
    );

    return Date.UTC(
      partes.get('year')!,
      partes.get('month')! - 1,
      partes.get('day')!,
      partes.get('hour')!,
      partes.get('minute')!,
      partes.get('second')!,
    );
  }

  private convertirFechaLocal(fechaHora: string): number {
    // La API serializa LocalDateTime de Java sin zona horaria.
    return Date.parse(`${fechaHora}Z`);
  }
}
