import { Component, computed, ElementRef, inject, signal, viewChild } from '@angular/core';
import { FormField, form, min, required, submit, validate } from '@angular/forms/signals';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom, forkJoin } from 'rxjs';
import { PeliculaResponse } from '../../models/pelicula-detalle.model';
import { Sala } from '../../models/sala.model';
import { SesionGestion } from '../../models/sesion.model';
import { PeliculaService } from '../../services/peliculaService';
import { SesionRequest, SesionService } from '../../services/sesionService';

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

@Component({
  selector: 'app-gestion-sesiones',
  standalone: true,
  imports: [FormField],
  templateUrl: './gestion-sesiones.html',
  styleUrl: './gestion-sesiones.scss',
})
export class GestionSesiones {
  private readonly sesionesApi = inject(SesionService);
  private readonly peliculasApi = inject(PeliculaService);
  private readonly panelFormulario = viewChild<ElementRef<HTMLElement>>('panelFormulario');

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

  readonly peliculasDisponibles = computed(() => {
    const idSeleccionado = this.modelo().peliculaId;
    return this.peliculas().filter(pelicula => pelicula.activo || String(pelicula.id) === idSeleccionado);
  });

  readonly sesionesDelDia = computed(() =>
    this.sesiones().filter(sesion => sesion.fechaHoraInicio.slice(0, 10) === this.fechaListado())
  );

  readonly fechaFormateada = computed(() => {
    const [anio, mes, dia] = this.fechaListado().split('-').map(Number);
    return new Date(anio, mes - 1, dia, 12).toLocaleDateString('es-ES', {
      day: 'numeric',
      month: 'long',
    });
  });

  constructor() {
    void this.cargarDatos();
  }

  async cargarDatos(): Promise<void> {
    this.cargando.set(true);
    this.mensajeError.set('');
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
    } catch {
      this.mensajeError.set('No se pudieron cargar las películas, salas y sesiones. Inténtalo de nuevo.');
    } finally {
      this.cargando.set(false);
    }
  }

  actualizarFechaListado(evento: Event): void {
    this.fechaListado.set((evento.target as HTMLInputElement).value);
  }

  formatearPrecio(precio: number): string {
    return precio.toLocaleString('es-ES', { style: 'currency', currency: 'EUR' });
  }

  mensajeDelError(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const cuerpo = error.error as { error?: unknown; detail?: unknown } | null;
      if (typeof cuerpo?.error === 'string') return cuerpo.error;
      if (typeof cuerpo?.detail === 'string') return cuerpo.detail;
    }
    return 'No se pudo guardar la sesión. Comprueba los datos e inténtalo de nuevo.';
  }

  editar(sesion: SesionGestion): void {
    this.sesionEditadaId.set(sesion.id);
    this.mensaje.set('');
    this.mensajeError.set('');
    this.modelo.set({
      peliculaId: String(sesion.peliculaId),
      fecha: sesion.fechaHoraInicio.slice(0, 10),
      hora: sesion.fechaHoraInicio.slice(11, 16),
      salaId: String(sesion.salaId),
      precioBase: sesion.precioBase,
    });
    requestAnimationFrame(() => {
      this.panelFormulario()?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
  }

  nuevaSesion(): void {
    this.sesionEditadaId.set(null);
    this.mensaje.set('');
    this.mensajeError.set('');
    const manana = new Date();
    manana.setDate(manana.getDate() + 1);
    this.modelo.set({
      peliculaId: String(this.peliculas().find(pelicula => pelicula.activo)?.id ?? ''),
      fecha: fechaLocal(manana),
      hora: '16:00',
      salaId: String(this.salas()[0]?.id ?? ''),
      precioBase: 8.9,
    });
  }

  async guardar(): Promise<void> {
    this.mensaje.set('');
    this.mensajeError.set('');
    await submit(this.formulario, async () => {
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
        this.nuevaSesion();
        this.fechaListado.set(datos.fecha);
        this.mensaje.set('La sesión se ha guardado correctamente.');
        await this.cargarDatos();
      } catch (error) {
        this.mensajeError.set(this.mensajeDelError(error));
      } finally {
        this.guardando.set(false);
      }
    });
  }
}
