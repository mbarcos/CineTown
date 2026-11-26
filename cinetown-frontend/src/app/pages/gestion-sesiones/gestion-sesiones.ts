import { NgTemplateOutlet } from '@angular/common';
import { afterNextRender, Component, computed, DestroyRef, ElementRef, inject, Injector, signal, viewChild } from '@angular/core';
import { FieldState, FormField, form, min, required, submit, validate } from '@angular/forms/signals';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom, forkJoin } from 'rxjs';
import { PeliculaResponse } from '../../models/pelicula-detalle.model';
import { Sala } from '../../models/sala.model';
import { SesionGestion } from '../../models/sesion.model';
import { PeliculaService } from '../../services/peliculaService';
import { SesionRequest, SesionService } from '../../services/sesionService';
import { Selector } from '../../components/selector/selector';

interface SesionFormModel {
  peliculaId: string;
  fecha: string;
  hora: string;
  salaId: string;
  precioBase: number;
}

function fechaLocal(fecha: Date): string {
  const anio = fecha.getFullYear();
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  return `${anio}-${mes}-${dia}`;
}

function fechaMananaLocal(): string {
  const manana = new Date();
  manana.setDate(manana.getDate() + 1);
  return fechaLocal(manana);
}

function descomponerFechaHora(valor: string): { fecha: string; hora: string } {
  const fechaHora = valor.trim();
  const tieneZonaHoraria = /(?:Z|[+-]\d{2}:\d{2})$/i.test(fechaHora);

  if (tieneZonaHoraria) {
    const fechaLocalizada = new Date(fechaHora);
    if (!Number.isNaN(fechaLocalizada.getTime())) {
      return {
        fecha: fechaLocal(fechaLocalizada),
        hora: `${String(fechaLocalizada.getHours()).padStart(2, '0')}:${String(fechaLocalizada.getMinutes()).padStart(2, '0')}`,
      };
    }
  }

  const [fecha, horaCompleta = ''] = fechaHora.split(/[T ]/);
  const hora = horaCompleta.match(/^(\d{2}:\d{2})/)?.[1];
  if (!fecha || !hora) {
    throw new Error('La fecha y hora de la sesión tienen un formato no válido.');
  }
  return { fecha, hora };
}

@Component({
  selector: 'app-gestion-sesiones',
  standalone: true,
  imports: [Selector, FormField, NgTemplateOutlet],
  templateUrl: './gestion-sesiones.html',
  styleUrl: './gestion-sesiones.scss',
})
export class GestionSesiones {
  private readonly sesionesApi = inject(SesionService);
  private readonly peliculasApi = inject(PeliculaService);
  private readonly injector = inject(Injector);
  private readonly elementoFormulario = viewChild.required<ElementRef<HTMLFormElement>>('elementoFormulario');
  private readonly tituloFormulario = viewChild.required<ElementRef<HTMLHeadingElement>>('tituloFormulario');
  private readonly dialogoEditor = viewChild<ElementRef<HTMLDialogElement>>('dialogoEditor');
  private readonly tituloAgenda = viewChild.required<ElementRef<HTMLHeadingElement>>('tituloAgenda');
  private readonly agendaCuerpo = viewChild.required<ElementRef<HTMLElement>>('agendaCuerpo');
  private readonly botonGuardar = viewChild.required<ElementRef<HTMLButtonElement>>('botonGuardar');
  private origenFormulario: HTMLButtonElement | undefined;

  private readonly modelo = signal<SesionFormModel>({
    peliculaId: '',
    fecha: fechaMananaLocal(),
    hora: '16:00',
    salaId: '',
    precioBase: 8.9,
  });

  readonly formulario = form(this.modelo, path => {
    required(path.peliculaId, { message: 'Selecciona una película.' });
    required(path.fecha, { message: 'Selecciona una fecha.' });
    required(path.hora, { message: 'Selecciona una hora.' });
    validate(path.hora, ({ valueOf }) => {
      const inicio = new Date(`${valueOf(path.fecha)}T${valueOf(path.hora)}:00`);
      return Number.isNaN(inicio.getTime()) || inicio.getTime() > Date.now()
        ? undefined
        : { kind: 'fechaPasada', message: 'La fecha y hora deben ser futuras.' };
    });
    required(path.salaId, { message: 'Selecciona una sala.' });
    required(path.precioBase, { message: 'Indica el precio.' });
    min(path.precioBase, 0, { message: 'El precio no puede ser negativo.' });
  });

  readonly peliculas = signal<PeliculaResponse[]>([]);
  readonly salas = signal<Sala[]>([]);
  readonly sesiones = signal<SesionGestion[]>([]);
  readonly fechaListado = signal(fechaLocal(new Date()));
  readonly sesionEditadaId = signal<number | null>(null);
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly mensaje = signal('');
  readonly mensajeError = signal('');
  readonly errorCarga = signal('');
  readonly modoCompacto = signal(false);

  readonly peliculasDisponibles = computed(() => {
    const idSeleccionado = this.modelo().peliculaId;
    return this.peliculas().filter(pelicula => pelicula.activo || String(pelicula.id) === idSeleccionado);
  });

  readonly sesionesDelDia = computed(() =>
    this.sesiones().filter(sesion => sesion.fechaHoraInicio.slice(0, 10) === this.fechaListado())
      .sort((a, b) => a.fechaHoraInicio.localeCompare(b.fechaHoraInicio) || a.numeroSala - b.numeroSala)
  );

  readonly fechaFormateada = computed(() => {
    const fecha = this.fechaListado();
    if (!fecha) return '';
    return new Date(`${fecha}T12:00:00`).toLocaleDateString('es-ES', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
    });
  });

  readonly puedeGuardar = computed(() =>
    !this.cargando() && !this.guardando() && this.peliculasDisponibles().length > 0 && this.salas().length > 0
  );

  constructor() {
    const destroyRef = inject(DestroyRef);
    afterNextRender(() => {
      const consulta = window.matchMedia?.('(width <= 59rem), (height < 48rem)');
      if (!consulta) return;
      const actualizarModo = () => {
        const editorEnUso = this.tituloFormulario().nativeElement.closest('.editor')?.contains(document.activeElement)
          || this.sesionEditadaId() !== null;
        this.dialogoEditor()?.nativeElement.close();
        this.modoCompacto.set(consulta.matches);
        if (editorEnUso) this.mostrarFormulario();
      };
      actualizarModo();
      consulta.addEventListener('change', actualizarModo);
      destroyRef.onDestroy(() => consulta.removeEventListener('change', actualizarModo));
    });
    void this.cargarDatos();
  }

  async cargarDatos(): Promise<void> {
    this.cargando.set(true);
    this.errorCarga.set('');
    try {
      const { peliculas, salas, sesiones } = await firstValueFrom(forkJoin({
        peliculas: this.peliculasApi.getPeliculasParaGestion(),
        salas: this.sesionesApi.consultarSalas(),
        sesiones: this.sesionesApi.consultarSesionesParaGestion(),
      }));
      this.peliculas.set(peliculas);
      this.salas.set(salas);
      this.sesiones.set(sesiones);
      if (this.modelo().peliculaId === '') {
        this.modelo.update(modelo => ({
          ...modelo,
          peliculaId: String(peliculas.find(pelicula => pelicula.activo)?.id ?? ''),
          salaId: String(salas[0]?.id ?? ''),
        }));
      }
    } catch (error) {
      this.errorCarga.set(this.mensajeDelError(error, 'No se pudo cargar la agenda. Inténtalo de nuevo.'));
    } finally {
      this.cargando.set(false);
    }
  }

  actualizarFechaListado(evento: Event): void {
    this.fechaListado.set((evento.target as HTMLInputElement).value);
    this.agendaCuerpo().nativeElement.scrollTop = 0;
  }

  reintentarCarga(): void {
    this.tituloAgenda().nativeElement.focus();
    void this.cargarDatos();
  }

  errorDe<T>(campo: FieldState<T>): string | undefined {
    return campo.touched() ? campo.errors()[0]?.message : undefined;
  }

  formatearPrecio(precio: number): string {
    return precio.toLocaleString('es-ES', { style: 'currency', currency: 'EUR' });
  }

  mensajeDelError(error: unknown, alternativa = 'No se pudo guardar la sesión. Comprueba los datos e inténtalo de nuevo.'): string {
    if (error instanceof HttpErrorResponse) {
      const cuerpo = error.error as { error?: unknown; detail?: unknown; errores?: Record<string, unknown> } | null;
      if (typeof cuerpo?.error === 'string') return cuerpo.error;
      if (typeof cuerpo?.detail === 'string') {
        const errores = Object.values(cuerpo.errores ?? {}).filter((valor): valor is string => typeof valor === 'string');
        return errores.length ? `${cuerpo.detail}: ${errores.join(' ')}` : cuerpo.detail;
      }
      if (error.status === 0) return 'No se pudo conectar con el servidor. Revisa tu conexión e inténtalo de nuevo.';
    }
    return error instanceof Error ? error.message : alternativa;
  }

  editar(sesion: SesionGestion, origen: HTMLButtonElement): void {
    if (this.guardando()) return;
    this.mensaje.set('');
    this.mensajeError.set('');
    let partesFechaHora: { fecha: string; hora: string };
    try {
      partesFechaHora = descomponerFechaHora(sesion.fechaHoraInicio);
    } catch (error) {
      this.mensajeError.set(this.mensajeDelError(error, 'No se pudo cargar la hora de esta sesión.'));
      this.mostrarFormulario();
      return;
    }
    this.origenFormulario = origen;
    this.sesionEditadaId.set(sesion.id);
    this.formulario().reset({
      peliculaId: String(sesion.peliculaId),
      fecha: partesFechaHora.fecha,
      hora: partesFechaHora.hora,
      salaId: String(sesion.salaId),
      precioBase: sesion.precioBase,
    });
    this.mostrarFormulario();
  }

  nuevaSesion(origen: HTMLButtonElement): void {
    this.restablecerFormulario();
    this.origenFormulario = origen;
    this.mostrarFormulario();
  }

  private mostrarFormulario(): void {
    afterNextRender(() => {
      const dialogo = this.dialogoEditor()?.nativeElement;
      if (dialogo && !dialogo.open) dialogo.showModal();
      this.tituloFormulario().nativeElement.focus();
    }, { injector: this.injector });
  }

  cancelarFormulario(evento?: Event): void {
    evento?.preventDefault();
    if (this.guardando()) return;
    const origen = this.origenFormulario;
    this.dialogoEditor()?.nativeElement.close();
    this.restablecerFormulario();
    if (origen?.isConnected) origen.focus();
    else if (this.modoCompacto()) this.tituloAgenda().nativeElement.focus();
    else this.tituloFormulario().nativeElement.focus();
  }

  private restablecerFormulario(): void {
    this.origenFormulario = undefined;
    this.sesionEditadaId.set(null);
    this.mensaje.set('');
    this.mensajeError.set('');
    this.formulario().reset({
      peliculaId: String(this.peliculas().find(pelicula => pelicula.activo)?.id ?? ''),
      fecha: fechaMananaLocal(),
      hora: '16:00',
      salaId: String(this.salas()[0]?.id ?? ''),
      precioBase: 8.9,
    });
  }

  async guardar(evento: Event): Promise<void> {
    evento.preventDefault();
    if (!this.puedeGuardar()) return;
    this.mensaje.set('');
    this.mensajeError.set('');
    const guardado = await submit(this.formulario, async () => {
      this.botonGuardar().nativeElement.focus();
      this.guardando.set(true);
      const datos = this.modelo();
      const solicitud: SesionRequest = {
        peliculaId: Number(datos.peliculaId),
        salaId: Number(datos.salaId),
        fechaHoraInicio: `${datos.fecha}T${datos.hora}:00`,
        precioBase: datos.precioBase,
      };
      try {
        const id = this.sesionEditadaId();
        if (id === null) {
          await firstValueFrom(this.sesionesApi.programarSesion(solicitud));
        } else {
          await firstValueFrom(this.sesionesApi.modificarSesion(id, solicitud));
        }
        this.restablecerFormulario();
        this.fechaListado.set(datos.fecha);
        this.agendaCuerpo().nativeElement.scrollTop = 0;
        this.mensaje.set(id === null ? 'Sesión programada.' : 'Cambios guardados.');
        if (this.modoCompacto()) {
          this.dialogoEditor()?.nativeElement.close();
          this.tituloAgenda().nativeElement.focus();
        }
        await this.cargarDatos();
      } catch (error) {
        this.mensajeError.set(this.mensajeDelError(error));
      } finally {
        this.guardando.set(false);
      }
    });
    if (!guardado) {
      afterNextRender(() => {
        this.elementoFormulario().nativeElement.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus();
      }, { injector: this.injector });
    }
  }
}
