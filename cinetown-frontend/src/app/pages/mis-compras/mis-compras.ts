import { afterNextRender, Component, computed, ElementRef, inject, Injector, signal, viewChild, viewChildren } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { LucideArrowLeft, LucideArrowRight } from '@lucide/angular';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../services/authService';
import { CompraResumen, CompraService } from '../../services/compraService';

@Component({
  selector: 'app-mis-compras', standalone: true,
  imports: [LucideArrowLeft, LucideArrowRight],
  templateUrl: './mis-compras.html', styleUrl: './mis-compras.scss',
})
export class MisCompras {
  private readonly comprasApi = inject(CompraService);
  private readonly auth = inject(AuthService);
  private readonly injector = inject(Injector);
  private readonly encabezado = viewChild.required<ElementRef<HTMLHeadingElement>>('encabezado');
  private readonly conservar = viewChild<ElementRef<HTMLButtonElement>>('conservar');
  private readonly titulosCompras = viewChildren<ElementRef<HTMLHeadingElement>>('tituloCompra');
  private readonly tamanoPagina = 10;
  private readonly formatoFecha = new Intl.DateTimeFormat('es-ES', {
    weekday: 'short', day: 'numeric', month: 'short', year: 'numeric',
  });
  private readonly formatoPrecio = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' });
  private origenCancelacion: HTMLButtonElement | undefined;
  readonly compras = signal<CompraResumen[]>([]);
  readonly pagina = signal(0);
  readonly totalPaginas = signal(0);
  readonly totalCompras = signal(0);
  readonly cargando = signal(true);
  readonly error = signal('');
  readonly errorCancelacion = signal('');
  readonly mensaje = signal('');
  readonly compraPendiente = signal<string | null>(null);
  readonly compraCancelando = signal<string | null>(null);
  readonly puedeCancelar = computed(() => this.auth.tieneAlgunRol(['CLIENTE', 'ADMIN', 'TAQUILLA']));
  readonly titulo = computed(() => this.auth.tieneAlgunRol(['ADMIN', 'TAQUILLA']) ? 'Compras' : 'Mis compras');
  readonly esPersonal = computed(() => this.auth.tieneAlgunRol(['ADMIN', 'TAQUILLA']));

  constructor() { void this.cargarCompras(); }

  async cargarCompras(mostrarCarga = true): Promise<void> {
    if (mostrarCarga) this.cargando.set(true);
    this.error.set('');
    try {
      const resultado = await firstValueFrom(this.comprasApi.consultarCompras(this.pagina(), this.tamanoPagina));
      this.compras.set(resultado.content);
      this.totalPaginas.set(resultado.totalPages);
      this.totalCompras.set(resultado.totalElements);
    } catch (error) {
      if (mostrarCarga) this.compras.set([]);
      this.error.set(this.mensajeDeError(error));
    } finally { this.cargando.set(false); }
  }

  async cambiarPagina(delta: number): Promise<void> {
    const nuevaPagina = this.pagina() + delta;
    if (this.cargando() || this.compraCancelando() || nuevaPagina < 0 || nuevaPagina >= this.totalPaginas()) return;
    this.pagina.set(nuevaPagina);
    this.limpiarConfirmacion();
    this.mensaje.set('');
    this.encabezado().nativeElement.focus({ preventScroll: true });
    await this.cargarCompras();
  }

  reintentarCarga(): void {
    this.encabezado().nativeElement.focus({ preventScroll: true });
    void this.cargarCompras();
  }

  pedirCancelacion(codigo: string, origen: HTMLButtonElement): void {
    if (this.compraCancelando()) return;
    if (this.compraPendiente() === codigo) {
      this.conservarCompra();
      return;
    }
    this.mensaje.set('');
    this.errorCancelacion.set('');
    this.origenCancelacion = origen;
    this.compraPendiente.set(codigo);
    afterNextRender(() => this.conservar()?.nativeElement.focus(), { injector: this.injector });
  }

  conservarCompra(): void {
    if (this.compraCancelando()) return;
    const origen = this.origenCancelacion;
    this.limpiarConfirmacion();
    if (origen?.isConnected) origen.focus();
    else this.encabezado().nativeElement.focus();
  }

  private limpiarConfirmacion(): void {
    this.compraPendiente.set(null);
    this.errorCancelacion.set('');
    this.origenCancelacion = undefined;
  }

  async cancelarCompra(compra: CompraResumen): Promise<void> {
    if (this.compraCancelando() || this.compraPendiente() !== compra.codCompra) return;
    if (!this.esCancelable(compra)) {
      this.errorCancelacion.set('Esta compra ya no se puede cancelar.');
      return;
    }
    this.compraCancelando.set(compra.codCompra);
    this.errorCancelacion.set('');
    this.mensaje.set('');
    try {
      await firstValueFrom(this.comprasApi.cancelarCompra(compra.codCompra));
      this.limpiarConfirmacion();
      this.compras.update(compras => compras.map(actual =>
        actual.codCompra === compra.codCompra ? { ...actual, estado: false } : actual,
      ));
      this.mensaje.set(`La compra ${compra.codCompra} se ha cancelado.`);
      afterNextRender(() => {
        const titulo = this.titulosCompras().find(({ nativeElement }) => nativeElement.dataset['compra'] === compra.codCompra);
        (titulo ?? this.encabezado()).nativeElement.focus();
      }, { injector: this.injector });
      await this.cargarCompras(false);
    } catch (error) {
      this.errorCancelacion.set(this.mensajeDeError(error, 'No se pudo cancelar la compra. Inténtalo de nuevo.'));
    } finally { this.compraCancelando.set(null); }
  }

  esCancelable(compra: CompraResumen): boolean {
    const fechaInicio = this.parsearFechaLocal(compra.fechaHoraInicio);
    return this.puedeCancelar() && compra.estado && fechaInicio.getTime() - Date.now() > 60 * 60 * 1000;
  }

  fechaLegible(valor: string): string {
    return this.formatoFecha.format(this.parsearFechaLocal(valor));
  }
  horaLegible(valor: string): string { return valor.slice(11, 16); }
  precioLegible(valor: number): string { return this.formatoPrecio.format(valor); }

  private parsearFechaLocal(valor: string): Date {
    const [fecha, hora = '00:00:00'] = valor.split(/[T ]/);
    const [anio, mes, dia] = fecha.split('-').map(Number);
    const [horas, minutos, segundos = 0] = hora.split(':').map(Number);
    return new Date(anio, mes - 1, dia, horas, minutos, segundos);
  }

  private mensajeDeError(error: unknown, alternativa = 'No se pudieron cargar las compras. Inténtalo de nuevo.'): string {
    if (error instanceof HttpErrorResponse) {
      const cuerpo = error.error as { message?: unknown; detail?: unknown; error?: unknown } | null;
      if (typeof cuerpo?.detail === 'string') return cuerpo.detail;
      if (typeof cuerpo?.message === 'string') return cuerpo.message;
      if (typeof cuerpo?.error === 'string') return cuerpo.error;
      if (error.status === 0) return 'No se pudo conectar con el servidor. Revisa tu conexión e inténtalo de nuevo.';
    }
    return alternativa;
  }
}
