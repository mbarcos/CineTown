import {
  afterNextRender,
  Component,
  computed,
  DestroyRef,
  ElementRef,
  inject,
  Injector,
  linkedSignal,
  viewChild,
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormField, email, form, readonly, required, submit } from '@angular/forms/signals';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { catchError, distinctUntilChanged, firstValueFrom, map, of, startWith, switchMap } from 'rxjs';
import { CompraResponse, CompraService } from '../../services/compraService';
import { SesionService } from '../../services/sesionService';

const MAX_ENTRADAS_POR_COMPRA = 10;

@Component({
  selector: 'app-compra',
  imports: [FormField, RouterLink],
  templateUrl: './compra.html',
  styleUrl: './compra.scss',
})
export class Compra {
  private readonly route = inject(ActivatedRoute);
  private readonly sesionService = inject(SesionService);
  private readonly compraService = inject(CompraService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly injector = inject(Injector);
  private readonly tituloCompra = viewChild.required<ElementRef<HTMLHeadingElement>>('tituloCompra');
  private readonly formatoPrecio = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' });
  private readonly formatoFecha = new Intl.DateTimeFormat('es-ES', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });

  private readonly idSesion = this.route.paramMap.pipe(
    map((params) => Number(params.get('id'))),
    distinctUntilChanged(),
  );

  protected readonly sesion = toSignal(
    this.idSesion.pipe(
      switchMap((id) => {
        if (!Number.isInteger(id) || id < 1) return of(null);
        return this.sesionService.consultarSesionPorId(id).pipe(
          startWith(undefined),
          catchError(() => of(null)),
        );
      }),
    ),
  );

  protected readonly cargandoSesion = computed(() => this.sesion() === undefined);
  protected readonly maxEntradas = computed(() =>
    Math.max(0, Math.min(MAX_ENTRADAS_POR_COMPRA, this.sesion()?.asientosLibres ?? 0)),
  );
  protected readonly limiteEntradas = computed(() => {
    const plazas = this.maxEntradas();
    if (plazas >= MAX_ENTRADAS_POR_COMPRA) return `Máximo ${MAX_ENTRADAS_POR_COMPRA} por compra`;
    return plazas === 1 ? 'Queda 1 entrada' : `Quedan ${plazas} entradas`;
  });

  protected readonly cantidad = linkedSignal({
    source: this.sesion,
    computation: (sesion): number => sesion && sesion.asientosLibres > 0 ? 1 : 0,
  });
  protected readonly compraRealizada = linkedSignal({
    source: this.sesion,
    computation: (): CompraResponse | null => null,
  });
  protected readonly errorCompra = linkedSignal({ source: this.sesion, computation: () => '' });

  private readonly datos = linkedSignal({ source: this.sesion, computation: () => ({ email: '' }) });
  protected readonly formulario = form(this.datos, (campos) => {
    required(campos.email, { message: 'Introduce tu correo electrónico.' });
    email(campos.email, { message: 'Introduce un correo electrónico válido.' });
    readonly(campos.email, { when: () => this.enviando() });
  });
  protected readonly enviando = computed(() => this.formulario().submitting());
  protected readonly errorCorreo = computed(() => {
    const correo = this.formulario.email();
    return correo.touched() ? correo.errors()[0]?.message : undefined;
  });

  private readonly precio = computed(() => {
    const sesion = this.sesion();
    return sesion?.precioEntrada ?? sesion?.precioBase ?? 0;
  });
  protected readonly precioUnitario = computed(() => this.formatearPrecio(this.precio()));
  protected readonly totalLegible = computed(() => this.formatearPrecio(this.precio() * this.cantidad()));
  protected readonly fechaLegible = computed(() => {
    const inicio = this.sesion()?.fechaHoraInicio;
    if (!inicio) return '';
    const fecha = this.formatoFecha.format(new Date(inicio));
    return `${fecha.charAt(0).toLocaleUpperCase('es-ES')}${fecha.slice(1)} a las ${inicio.slice(11, 16)}`;
  });

  protected formatearPrecio(total: number): string {
    return this.formatoPrecio.format(total);
  }

  protected cambiarCantidad(delta: number): void {
    if (this.enviando() || !this.maxEntradas()) return;
    this.cantidad.update((cantidad) => Math.max(1, Math.min(this.maxEntradas(), cantidad + delta)));
  }

  protected async confirmarCompra(evento: Event, correo: HTMLInputElement): Promise<void> {
    evento.preventDefault();
    const sesion = this.sesion();
    if (!sesion || this.enviando() || !this.maxEntradas() || this.compraRealizada()) return;

    this.errorCompra.set('');
    await submit(this.formulario, async () => {
      try {
        const compra = await firstValueFrom(
          this.compraService.comprarEntradas({
            sesionId: sesion.id,
            numEntradas: this.cantidad(),
            email: this.datos().email.trim(),
          }).pipe(takeUntilDestroyed(this.destroyRef)),
        );

        if (this.sesion() !== sesion) return;
        this.compraRealizada.set(compra);
        afterNextRender({ write: () => this.tituloCompra().nativeElement.focus() }, { injector: this.injector });
      } catch (error) {
        if (this.destroyRef.destroyed || this.sesion() !== sesion) return;
        const detalle: unknown = error instanceof HttpErrorResponse ? error.error?.detail : undefined;
        this.errorCompra.set(
          typeof detalle === 'string' && detalle
            ? detalle
            : 'No se pudo completar la compra. Inténtalo de nuevo.',
        );
      }
    });

    if (correo.isConnected && this.errorCorreo()) correo.focus();
  }
}
