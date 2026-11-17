import { Component, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../services/authService';
import { CompraResumen, CompraService } from '../../services/compraService';

@Component({
  selector: 'app-mis-compras', standalone: true,
  templateUrl: './mis-compras.html', styleUrl: './mis-compras.scss',
})
export class MisCompras {
  private readonly comprasApi = inject(CompraService);
  private readonly auth = inject(AuthService);
  private readonly tamanoPagina = 10;
  readonly compras = signal<CompraResumen[]>([]);
  readonly pagina = signal(0);
  readonly totalPaginas = signal(0);
  readonly totalCompras = signal(0);
  readonly cargando = signal(true);
  readonly error = signal('');
  readonly mensaje = signal('');
  readonly compraPendiente = signal<string | null>(null);
  readonly compraCancelando = signal<string | null>(null);
  readonly puedeCancelar = computed(() => this.auth.tieneAlgunRol(['CLIENTE', 'ADMIN', 'TAQUILLA']));
  readonly titulo = computed(() => this.auth.tieneAlgunRol(['ADMIN', 'TAQUILLA']) ? 'Compras' : 'Mis compras');
  readonly esPersonal = computed(() => this.auth.tieneAlgunRol(['ADMIN', 'TAQUILLA']));

  constructor() { void this.cargarCompras(); }

  async cargarCompras(): Promise<void> {
    this.cargando.set(true);
    this.error.set('');
    try {
      const resultado = await firstValueFrom(this.comprasApi.consultarCompras(this.pagina(), this.tamanoPagina));
      this.compras.set(resultado.content);
      this.totalPaginas.set(resultado.totalPages);
      this.totalCompras.set(resultado.totalElements);
    } catch (error) {
      this.error.set(this.mensajeDeError(error));
    } finally { this.cargando.set(false); }
  }

  async cambiarPagina(delta: number): Promise<void> {
    const nuevaPagina = this.pagina() + delta;
    if (nuevaPagina < 0 || nuevaPagina >= this.totalPaginas()) return;
    this.pagina.set(nuevaPagina);
    this.compraPendiente.set(null);
    await this.cargarCompras();
  }

  pedirCancelacion(codigo: string): void {
    this.mensaje.set('');
    this.compraPendiente.set(codigo);
  }

  async cancelarCompra(compra: CompraResumen): Promise<void> {
    if (this.compraCancelando() || !this.esCancelable(compra)) return;
    this.compraCancelando.set(compra.codCompra);
    this.error.set('');
    this.mensaje.set('');
    try {
      await firstValueFrom(this.comprasApi.cancelarCompra(compra.codCompra));
      this.compraPendiente.set(null);
      this.mensaje.set(`La compra ${compra.codCompra} se ha cancelado.`);
      await this.cargarCompras();
    } catch (error) {
      this.error.set(this.mensajeDeError(error, 'No se pudo cancelar la compra. Comprueba las condiciones e inténtalo de nuevo.'));
    } finally { this.compraCancelando.set(null); }
  }

  esCancelable(compra: CompraResumen): boolean {
    const fechaInicio = this.parsearFechaLocal(compra.fechaHoraInicio);
    return this.puedeCancelar() && compra.estado && fechaInicio.getTime() - Date.now() > 60 * 60 * 1000;
  }

  fechaLegible(valor: string): string {
    return new Intl.DateTimeFormat('es-ES', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' })
      .format(this.parsearFechaLocal(valor));
  }
  horaLegible(valor: string): string { return valor.slice(11, 16); }
  precioLegible(valor: number): string { return valor.toLocaleString('es-ES', { style: 'currency', currency: 'EUR' }); }

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
