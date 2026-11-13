import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, combineLatest, map, of, switchMap, tap } from 'rxjs';
import { SesionConAsientos } from '../../models/sesion.model';
import { PeliculaService } from '../../services/peliculaService';
import { SesionService } from '../../services/sesionService';

interface SesionPresentacion extends SesionConAsientos {
  horaLegible: string;
  precioLegible: string;
}

@Component({
  selector: 'app-sesiones-pelicula',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './sesiones-pelicula.html',
  styleUrl: './sesiones-pelicula.scss',
})
export class SesionesPelicula {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly peliculaService = inject(PeliculaService);
  private readonly sesionService = inject(SesionService);

  private readonly fechaActualMadrid = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Europe/Madrid',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
  private readonly fechaCortaFormatter = new Intl.DateTimeFormat('es-ES', {
    timeZone: 'UTC',
    day: 'numeric',
    month: 'short',
  });
  private readonly fechaLargaFormatter = new Intl.DateTimeFormat('es-ES', {
    timeZone: 'UTC',
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });
  private readonly precioFormatter = new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
  });

  readonly hoy = this.fechaISO(new Date());
  readonly manana = this.sumarDias(this.hoy, 1);
  readonly cargando = signal(true);
  readonly error = signal(false);
  readonly sesionSeleccionadaId = signal<number | null>(null);
  private readonly reintento = signal(0);

  readonly pelicula = toSignal(
    this.route.paramMap.pipe(
      map((params) => Number(params.get('id'))),
      switchMap((id) => this.peliculaService.obtenerDetalle(id)),
    ),
  );

  readonly fechaSeleccionada = toSignal(
    this.route.queryParamMap.pipe(map((params) => params.get('fecha') || this.hoy)),
    { initialValue: this.hoy },
  );

  readonly sesiones = toSignal(
    combineLatest([
      this.route.paramMap,
      this.route.queryParamMap,
      toObservable(this.reintento),
    ]).pipe(
      tap(() => {
        this.cargando.set(true);
        this.error.set(false);
      }),
      switchMap(([params, query]) => {
        const peliculaId = Number(params.get('id'));
        const fecha = query.get('fecha') || this.hoy;

        return this.sesionService
          .consultarSesionesActivasPorPeliculaYFecha(peliculaId, fecha)
          .pipe(
            tap(() => this.cargando.set(false)),
            catchError(() => {
              this.cargando.set(false);
              this.error.set(true);
              return of([] as SesionConAsientos[]);
            }),
          );
      }),
    ),
  );

  readonly sesionesPresentacion = computed<SesionPresentacion[]>(() =>
    (this.sesiones() ?? []).map((sesion) => ({
      ...sesion,
      horaLegible: sesion.fechaHoraInicio.slice(11, 16),
      precioLegible: this.precioFormatter.format(sesion.precioBase),
    })),
  );

  readonly sesionSeleccionada = computed(
    () => this.sesionesPresentacion().find((sesion) => sesion.id === this.sesionSeleccionadaId()) ?? null,
  );

  readonly fechaLegible = computed(() => {
    const fecha = this.fechaSeleccionada();
    const legible = this.fechaLargaFormatter.format(new Date(`${fecha}T00:00:00Z`));
    return legible.charAt(0).toLocaleUpperCase('es-ES') + legible.slice(1);
  });

  readonly fechasRapidas = computed(() => [
    { fecha: this.hoy, etiqueta: 'Hoy', detalle: this.etiquetaFecha(this.hoy) },
    { fecha: this.manana, etiqueta: 'Mañana', detalle: this.etiquetaFecha(this.manana) },
  ]);

  cambiarFecha(fecha: string): void {
    if (!fecha) return;
    this.sesionSeleccionadaId.set(null);
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { fecha },
      queryParamsHandling: 'merge',
    });
  }

  seleccionarSesion(id: number): void {
    this.sesionSeleccionadaId.set(id);
  }

  continuarCompra(): void {
    const sesion = this.sesionSeleccionada();
    if (sesion) void this.router.navigate(['/sesiones', sesion.id]);
  }

  recargarSesiones(): void {
    this.reintento.update((valor) => valor + 1);
  }

  esHoy(fecha: string): boolean {
    return fecha === this.hoy;
  }

  esManana(fecha: string): boolean {
    return fecha === this.manana;
  }

  private etiquetaFecha(fecha: string): string {
    return this.fechaCortaFormatter.format(new Date(`${fecha}T00:00:00Z`)).replace('.', '').toLocaleUpperCase('es-ES');
  }

  private fechaISO(fecha: Date): string {
    const partes = new Map(
      this.fechaActualMadrid
        .formatToParts(fecha)
        .filter((parte) => parte.type !== 'literal')
        .map((parte) => [parte.type, parte.value]),
    );
    return `${partes.get('year')}-${partes.get('month')}-${partes.get('day')}`;
  }

  private sumarDias(fecha: string, dias: number): string {
    const fechaUTC = new Date(`${fecha}T00:00:00Z`);
    fechaUTC.setUTCDate(fechaUTC.getUTCDate() + dias);
    return this.fechaISO(fechaUTC);
  }
}
