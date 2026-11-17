import { Component, computed, ElementRef, inject, signal, viewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, Observable } from 'rxjs';
import { PeliculaResponse } from '../../models/pelicula-detalle.model';
import { PeliculaRequest, PeliculaService } from '../../services/peliculaService';

@Component({
  selector: 'app-gestion-peliculas',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './gestion-peliculas.html',
  styleUrl: './gestion-peliculas.scss',
})
export class GestionPeliculas {
  private readonly panelEdicion = viewChild<ElementRef<HTMLElement>>('panelEdicion');
  private readonly peliculasApi = inject(PeliculaService);
  private readonly formBuilder = inject(FormBuilder);

  readonly peliculas = signal<PeliculaResponse[]>([]);
  readonly generos = signal<string[]>([]);
  readonly busqueda = signal('');
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly mensajeError = signal('');
  readonly formularioVisible = signal(false);
  readonly peliculaEditadaId = signal<number | null>(null);

  readonly peliculasFiltradas = computed(() => {
    const termino = this.busqueda().trim().toLocaleLowerCase();
    return this.peliculas().filter(pelicula =>
      !termino || pelicula.titulo.toLocaleLowerCase().includes(termino) ||
      pelicula.generos.some(genero => genero.toLocaleLowerCase().includes(termino))
    );
  });

  readonly formulario = this.formBuilder.nonNullable.group({
    titulo: ['', [Validators.required, Validators.maxLength(255)]],
    sinopsis: [''],
    duracion: [90, [Validators.required, Validators.min(1), Validators.max(300)]],
    anio: [new Date().getFullYear(), [Validators.required, Validators.min(1888), Validators.max(2100)]],
    clasificacion: ['TP' as PeliculaResponse['clasificacion'], Validators.required],
    generos: this.formBuilder.nonNullable.control<string[]>([], Validators.required),
  });

  constructor() {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando.set(true);
    this.mensajeError.set('');
    this.peliculasApi.getPeliculasParaGestion().pipe(
      finalize(() => this.cargando.set(false))
    ).subscribe({
      next: peliculas => this.peliculas.set(peliculas),
      error: () => this.mensajeError.set('No se pudieron cargar las películas. Inténtalo de nuevo.'),
    });
    this.peliculasApi.getGeneros().subscribe({
      next: generos => this.generos.set(generos),
      error: () => this.mensajeError.set('No se pudieron cargar los géneros disponibles.'),
    });
  }

  iniciarNuevaPelicula(): void {
    this.peliculaEditadaId.set(null);
    this.formulario.reset({
      titulo: '',
      sinopsis: '',
      duracion: 90,
      anio: new Date().getFullYear(),
      clasificacion: 'TP',
      generos: [],
    });
    this.mensajeError.set('');
    this.formularioVisible.set(true);
  }

  editar(pelicula: PeliculaResponse): void {
    this.peliculaEditadaId.set(pelicula.id);
    this.formulario.reset({
      titulo: pelicula.titulo,
      sinopsis: pelicula.sinopsis ?? '',
      duracion: pelicula.duracion,
      anio: pelicula.anio,
      clasificacion: pelicula.clasificacion,
      generos: [...pelicula.generos],
    });
    this.mensajeError.set('');
    this.formularioVisible.set(true);
    requestAnimationFrame(() => {
      this.panelEdicion()?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
  }

  alternarGenero(nombre: string, evento: Event): void {
    const seleccionado = (evento.target as HTMLInputElement).checked;
    const actual = this.formulario.controls.generos.value;
    this.formulario.controls.generos.setValue(
      seleccionado ? [...actual, nombre] : actual.filter(genero => genero !== nombre)
    );
    this.formulario.controls.generos.markAsTouched();
  }

  guardar(): void {
    this.formulario.markAllAsTouched();
    if (this.formulario.invalid || this.guardando()) return;

    const datos: PeliculaRequest = {
      ...this.formulario.getRawValue(),
      activo: this.peliculaEditadaId() === null
        ? true
        : this.peliculas().find(pelicula => pelicula.id === this.peliculaEditadaId())?.activo ?? true,
    };
    const id = this.peliculaEditadaId();
    this.guardando.set(true);
    this.mensajeError.set('');
    const solicitud: Observable<unknown> = id === null
      ? this.peliculasApi.crearPelicula(datos)
      : this.peliculasApi.actualizarPelicula(id, datos);

    solicitud.pipe(finalize(() => this.guardando.set(false))).subscribe({
      next: () => {
        this.formularioVisible.set(false);
        this.cargarDatos();
      },
      error: () => this.mensajeError.set('No se pudo guardar la película. Revisa los datos e inténtalo de nuevo.'),
    });
  }

  cambiarCartelera(pelicula: PeliculaResponse): void {
    this.mensajeError.set('');
    this.peliculasApi.cambiarEstadoCartelera(pelicula.id).subscribe({
      next: () => this.cargarDatos(),
      error: () => this.mensajeError.set(`No se pudo actualizar la cartelera de «${pelicula.titulo}».`),
    });
  }

  cancelarEdicion(): void {
    this.formularioVisible.set(false);
    this.peliculaEditadaId.set(null);
    this.mensajeError.set('');
  }

  actualizarBusqueda(evento: Event): void {
    this.busqueda.set((evento.target as HTMLInputElement).value);
  }
}
