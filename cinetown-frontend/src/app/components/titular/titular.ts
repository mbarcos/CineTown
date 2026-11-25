import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  afterNextRender,
  computed,
  effect,
  inject,
  input,
  signal,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { PeliculaResponse } from '../../models/pelicula-detalle.model';

@Component({
  selector: 'app-titular',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './titular.html',
  styleUrl: './titular.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TitularComponent {
  private readonly destroyRef = inject(DestroyRef);
  protected readonly rotacionDisponible = signal(false);
  readonly peliculas = input<readonly PeliculaResponse[]>([]);
  readonly intervaloMs = input<number>(5000);
  readonly titulo = input<string>('Cartelera');
  protected readonly peliculasValidas = computed(() =>
    this.peliculas().filter(
      (pelicula) =>
        (pelicula.activo ?? true) && Boolean(pelicula.cartelUrl?.trim())
    )
  );
  private readonly indiceActual = signal(0);
  private readonly reinicioTemporizador = signal(0);
  private readonly enHover = signal(false);
  private readonly enFoco = signal(false);
  protected readonly pausaManual = signal(false);

  /** Garantiza un índice siempre válido aunque cambie el tamaño del array. */
  protected readonly indiceSeguro = computed(() => {
    const total = this.peliculasValidas().length;
    if (total === 0) {
      return 0;
    }
    return ((this.indiceActual() % total) + total) % total;
  });

  protected readonly pausado = computed(
    () => this.enHover() || this.enFoco() || this.pausaManual()
  );

  protected readonly desplazamientoTrack = computed(
    () => `translate3d(-${this.indiceSeguro() * 100}%, 0, 0)`
  );

  private inicioTouchX: number | null = null;

  constructor() {
    afterNextRender(() => {
      const preferencia = window.matchMedia?.('(prefers-reduced-motion: reduce)');
      const actualizarRotacion = () => this.rotacionDisponible.set(!preferencia?.matches);
      actualizarRotacion();
      preferencia?.addEventListener('change', actualizarRotacion);
      this.destroyRef.onDestroy(() =>
        preferencia?.removeEventListener('change', actualizarRotacion)
      );
    });

    effect((onCleanup) => {
      if (!this.rotacionDisponible()) {
        return;
      }

      const total = this.peliculasValidas().length;
      const pausado = this.pausado();
      const intervalo = Math.max(1500, this.intervaloMs());

      // Suscripción reactiva para reiniciar el intervalo tras navegación manual.
      this.reinicioTemporizador();

      if (total <= 1 || pausado) {
        return;
      }

      const timerId = window.setInterval(() => {
        this.indiceActual.update((actual) => (actual + 1) % total);
      }, intervalo);

      onCleanup(() => window.clearInterval(timerId));
    });
  }

  protected siguiente(): void {
    const total = this.peliculasValidas().length;
    if (total <= 1) {
      return;
    }
    this.indiceActual.set((this.indiceSeguro() + 1) % total);
    this.reiniciarTemporizador();
  }

  protected anterior(): void {
    const total = this.peliculasValidas().length;
    if (total <= 1) {
      return;
    }
    this.indiceActual.set((this.indiceSeguro() - 1 + total) % total);
    this.reiniciarTemporizador();
  }

  protected irA(indice: number): void {
    const total = this.peliculasValidas().length;
    if (indice < 0 || indice >= total) {
      return;
    }
    this.indiceActual.set(indice);
    this.reiniciarTemporizador();
  }

  protected alternarPausa(): void {
    this.pausaManual.update((estado) => !estado);
  }

  protected alEntrarRaton(): void {
    this.enHover.set(true);
  }

  protected alSalirRaton(): void {
    this.enHover.set(false);
  }

  protected alRecibirFoco(): void {
    this.enFoco.set(true);
  }

  protected alPerderFoco(evento: FocusEvent): void {
    const contenedor = evento.currentTarget as HTMLElement | null;
    const siguienteElemento = evento.relatedTarget as Node | null;
    if (!contenedor?.contains(siguienteElemento)) {
      this.enFoco.set(false);
    }
  }

  protected alIniciarTouch(evento: TouchEvent): void {
    this.inicioTouchX = evento.changedTouches[0]?.clientX ?? null;
  }

  protected alFinalizarTouch(evento: TouchEvent): void {
    if (this.inicioTouchX === null) {
      return;
    }

    const finX = evento.changedTouches[0]?.clientX ?? this.inicioTouchX;
    const diferencia = this.inicioTouchX - finX;
    this.inicioTouchX = null;

    if (Math.abs(diferencia) < 40) {
      return;
    }

    if (diferencia > 0) {
      this.siguiente();
    } else {
      this.anterior();
    }
  }

  protected formatearDuracion(minutos: number): string {
    const horas = Math.floor(minutos / 60);
    const resto = minutos % 60;
    if (horas === 0) {
      return `${resto} min`;
    }
    return resto > 0 ? `${horas} h ${resto} min` : `${horas} h`;
  }

  private reiniciarTemporizador(): void {
    this.reinicioTemporizador.update((valor) => valor + 1);
  }
}
