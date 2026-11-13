import { Component, computed, inject, signal } from '@angular/core';
import { FormField, email, required, form } from '@angular/forms/signals';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, map, of, switchMap } from 'rxjs';
import { SesionConAsientos } from '../../models/sesion.model';
import { CompraResponse, CompraService } from '../../services/compraService';
import { SesionService } from '../../services/sesionService';

@Component({
  selector: 'app-compra',
  standalone: true,
  imports: [FormField, RouterLink],
  templateUrl: './compra.html',
  styleUrl: './compra.scss',
})
export class Compra {
  private readonly route = inject(ActivatedRoute);
  private readonly sesionService = inject(SesionService);
  private readonly compraService = inject(CompraService);
  private readonly precioFormato = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' });
  private readonly fechaFormato = new Intl.DateTimeFormat('es-ES', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric',
  });

  readonly cantidad = signal(1);
  readonly datosFormulario = signal({ email: '' });
  readonly formulario = form(this.datosFormulario, (campos) => {
    required(campos.email, { message: 'Introduce el correo para enviar la confirmación.' });
    email(campos.email, { message: 'Introduce un correo electrónico válido.' });
  });
  readonly cargandoCompra = signal(false);
  readonly errorCompra = signal('');
  readonly compraRealizada = signal<CompraResponse | null>(null);

  readonly resultadoSesion = toSignal(
    this.route.paramMap.pipe(
      map((params) => Number(params.get('id'))),
      switchMap((id) => Number.isInteger(id) && id > 0
        ? this.sesionService.consultarSesionPorId(id).pipe(
            map((sesion) => ({ sesion, error: false })),
            catchError(() => of({ sesion: undefined, error: true })),
          )
        : of({ sesion: undefined, error: true })),
    ),
  );
  readonly sesion = computed(() => this.resultadoSesion()?.sesion);
  readonly cargandoSesion = computed(() => this.resultadoSesion() === undefined);
  readonly errorSesion = computed(() => this.resultadoSesion()?.error === true);
  readonly maxEntradas = computed(() => Math.min(10, this.sesion()?.asientosLibres ?? 1));
  readonly total = computed(() => (this.sesion()?.precioBase ?? 0) * this.cantidad());
  readonly precioUnitario = computed(() => this.precioFormato.format(this.sesion()?.precioBase ?? 0));
  readonly totalLegible = computed(() => this.precioFormato.format(this.total()));
  precioCompra(total: number): string { return this.precioFormato.format(total); }
  readonly fechaLegible = computed(() => {
    const inicio = this.sesion()?.fechaHoraInicio;
    if (!inicio) return '';
    const fecha = new Date(inicio);
    const fechaTxt = this.fechaFormato.format(fecha);
    return `${fechaTxt.charAt(0).toLocaleUpperCase('es-ES')}${fechaTxt.slice(1)} · ${inicio.slice(11, 16)}`;
  });

  cambiarCantidad(delta: number): void {
    this.cantidad.update((cantidad) => Math.max(1, Math.min(this.maxEntradas(), cantidad + delta)));
  }

  confirmarCompra(evento: Event): void {
    evento.preventDefault();
    this.formulario.email().markAsTouched();
    const sesion = this.sesion();
    if (this.formulario().invalid() || !sesion || this.cargandoCompra() || this.maxEntradas() < 1) return;

    this.cargandoCompra.set(true);
    this.errorCompra.set('');
    this.compraService.comprarEntradas({
      sesionId: sesion.id,
      numEntradas: this.cantidad(),
      email: this.datosFormulario().email.trim(),
    }).subscribe({
      next: (compra) => {
        this.compraRealizada.set(compra);
        this.cargandoCompra.set(false);
      },
      error: (error: { error?: { message?: string } }) => {
        this.errorCompra.set(error.error?.message ?? 'No se pudo completar la compra. Comprueba la disponibilidad e inténtalo de nuevo.');
        this.cargandoCompra.set(false);
      },
    });
  }
}
