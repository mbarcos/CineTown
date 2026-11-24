import { afterNextRender, Component, computed, ElementRef, inject, Injector, signal, viewChild } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, finalize, map, Observable, of, switchMap, timer } from 'rxjs';
import { PeliculaCardComponent } from '../../components/pelicula-card/pelicula-card';
import { PeliculaResponse } from '../../models/pelicula-detalle.model';
import { BusquedaCartel, PeliculaRequest, PeliculaService } from '../../services/peliculaService';

@Component({
  selector: 'app-gestion-peliculas',
  standalone: true,
  imports: [ReactiveFormsModule, PeliculaCardComponent],
  templateUrl: './gestion-peliculas.html',
  styleUrl: './gestion-peliculas.scss',
})
export class GestionPeliculas {
  private readonly tituloFormulario = viewChild<ElementRef<HTMLHeadingElement>>('tituloFormulario');
  private readonly nuevaPelicula = viewChild<ElementRef<HTMLButtonElement>>('nuevaPelicula');
  private readonly editor = viewChild<ElementRef<HTMLFormElement>>('editor');
  private readonly buscador = viewChild<ElementRef<HTMLInputElement>>('buscador');
  private readonly selectorCartelera = viewChild<ElementRef<HTMLSelectElement>>('estadoCartelera');
  private readonly injector = inject(Injector);
  private readonly peliculasApi = inject(PeliculaService);
  private readonly formBuilder = inject(FormBuilder);
  private origenEdicion?: HTMLButtonElement;

  readonly peliculas = signal<PeliculaResponse[]>([]);
  readonly generos = signal<string[]>([]);
  readonly busqueda = signal('');
  readonly filtroCartelera = signal('todas');
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly mensajeError = signal('');
  readonly formularioVisible = signal(false);
  readonly peliculaEditadaId = signal<number | null>(null);

  readonly peliculasFiltradas = computed(() => {
    const termino = this.busqueda().trim().toLocaleLowerCase();
    const filtro = this.filtroCartelera();
    return this.peliculas().filter(pelicula => {
      const coincideCartelera = filtro === 'todas' || pelicula.activo === (filtro === 'activas');
      return coincideCartelera && (!termino || pelicula.titulo.toLocaleLowerCase().includes(termino) ||
        pelicula.generos.some(genero => genero.toLocaleLowerCase().includes(termino)));
    });
  });

  readonly hayFiltros = computed(() => !!this.busqueda().trim() || this.filtroCartelera() !== 'todas');

  readonly formulario = this.formBuilder.nonNullable.group({
    titulo: ['', [Validators.required, Validators.maxLength(255)]],
    sinopsis: [''],
    duracion: [90, [Validators.required, Validators.min(1), Validators.max(300)]],
    anio: [new Date().getFullYear(), [Validators.required, Validators.min(1888), Validators.max(2100)]],
    clasificacion: ['TP' as PeliculaResponse['clasificacion'], Validators.required],
    generos: this.formBuilder.nonNullable.control<string[]>([], Validators.required),
  });

  private readonly borrador = toSignal(this.formulario.valueChanges, { initialValue: this.formulario.getRawValue() });
  private readonly peliculaEditada = computed(() =>
    this.peliculas().find(pelicula => pelicula.id === this.peliculaEditadaId()),
  );
  private readonly cartelGuardado = computed(() => {
    const original = this.peliculaEditada();
    const { titulo, anio } = this.borrador();
    return original && original.titulo.trim() === titulo?.trim() && original.anio === anio
      ? original.cartelUrl?.trim() || null : null;
  });

  private readonly busquedaCartel = computed<BusquedaCartel | null>(() => {
    if (!this.formularioVisible() || this.cartelGuardado()) return null;
    const { titulo, anio, sinopsis } = this.borrador();
    const nombre = titulo?.trim();
    if (!nombre || nombre.length > 255 || anio == null || !Number.isInteger(anio) || anio < 1888 || anio > 2100) return null;
    return { titulo: nombre, anio, sinopsis: sinopsis?.trim() ?? '' };
  }, { equal: (a, b) => a?.titulo === b?.titulo && a?.anio === b?.anio && a?.sinopsis === b?.sinopsis });

  private readonly resultadoCartel = toSignal(toObservable(this.busquedaCartel).pipe(
    switchMap(busqueda => busqueda ? timer(450).pipe(
      switchMap(() => this.peliculasApi.previsualizarCartel(busqueda)),
      map(({ cartelUrl }) => ({ busqueda, cartelUrl, error: false })),
      catchError(() => of({ busqueda, cartelUrl: null, error: true })),
    ) : of(null)),
  ));

  private readonly cartelEncontrado = computed(() => {
    const resultado = this.resultadoCartel();
    return resultado?.busqueda === this.busquedaCartel() ? resultado : null;
  });

  readonly tarjetaPrevia = computed(() => {
    const { titulo, anio, generos } = this.borrador();
    return {
      id: this.peliculaEditadaId() ?? 0,
      titulo: titulo?.trim() || 'Nueva película',
      anio: anio ?? null,
      generos: generos ?? [],
      cartelUrl: this.cartelGuardado() ?? this.cartelEncontrado()?.cartelUrl ?? null,
    };
  });

  readonly avisoCartel = computed(() => {
    if (this.cartelGuardado()) return '';
    if (!this.busquedaCartel()) return 'Introduce un título y un año válidos para buscar el cartel.';
    const resultado = this.cartelEncontrado();
    if (!resultado) return 'Buscando cartel…';
    if (resultado.error) return 'No se pudo buscar el cartel.';
    return resultado.cartelUrl ? '' : 'No se encontró un cartel.';
  });

  constructor() {
    this.cargarDatos();
  }

  cargarDatos(origen?: HTMLButtonElement): void {
    this.cargando.set(true);
    this.mensajeError.set('');
    this.peliculasApi.getPeliculasParaGestion().pipe(
      finalize(() => this.cargando.set(false))
    ).subscribe({
      next: peliculas => {
        const teniaFoco = origen && origen.ownerDocument.activeElement === origen;
        this.peliculas.set(peliculas);
        if (teniaFoco) {
          afterNextRender({ write: () => {
            if (!origen.isConnected) this.enfocar(this.selectorCartelera()?.nativeElement);
          } }, { injector: this.injector });
        }
      },
      error: () => this.mensajeError.set('No se pudieron cargar las películas. Inténtalo de nuevo.'),
    });
    this.peliculasApi.getGeneros().subscribe({
      next: generos => this.generos.set(generos),
      error: () => this.mensajeError.set('No se pudieron cargar los géneros disponibles.'),
    });
  }

  iniciarNuevaPelicula(origen: HTMLButtonElement): void {
    this.peliculaEditadaId.set(null);
    this.formulario.reset({
      titulo: '',
      sinopsis: '',
      duracion: 90,
      anio: new Date().getFullYear(),
      clasificacion: 'TP',
      generos: [],
    });
    this.abrirFormulario(origen);
  }

  editar(pelicula: PeliculaResponse, origen: HTMLButtonElement): void {
    this.peliculaEditadaId.set(pelicula.id);
    this.formulario.reset({
      titulo: pelicula.titulo,
      sinopsis: pelicula.sinopsis ?? '',
      duracion: pelicula.duracion,
      anio: pelicula.anio,
      clasificacion: pelicula.clasificacion,
      generos: [...pelicula.generos],
    });
    this.abrirFormulario(origen);
  }

  private abrirFormulario(origen: HTMLButtonElement): void {
    this.origenEdicion = origen;
    this.mensajeError.set('');
    this.formularioVisible.set(true);
    afterNextRender({ write: () => this.enfocar(this.tituloFormulario()?.nativeElement) }, { injector: this.injector });
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
    if (this.guardando()) return;
    this.formulario.markAllAsTouched();
    if (this.formulario.invalid) {
      afterNextRender({ write: () => {
        const primerError = this.editor()?.nativeElement.querySelector<HTMLElement>(
          '[aria-invalid="true"], fieldset[aria-describedby] input, #error-generos',
        );
        this.enfocar(primerError ?? undefined);
      } }, { injector: this.injector });
      return;
    }

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
        this.cerrarFormulario();
        this.cargarDatos();
      },
      error: () => this.mensajeError.set('No se pudo guardar la película. Revisa los datos e inténtalo de nuevo.'),
    });
  }

  cambiarCartelera(pelicula: PeliculaResponse, origen: HTMLButtonElement): void {
    this.mensajeError.set('');
    this.peliculasApi.cambiarEstadoCartelera(pelicula.id).subscribe({
      next: () => this.cargarDatos(origen),
      error: () => this.mensajeError.set(`No se pudo actualizar la cartelera de «${pelicula.titulo}».`),
    });
  }

  cancelarEdicion(): void {
    this.cerrarFormulario(this.origenEdicion);
    this.peliculaEditadaId.set(null);
    this.mensajeError.set('');
  }

  private cerrarFormulario(origen?: HTMLButtonElement): void {
    this.formularioVisible.set(false);
    this.origenEdicion = undefined;
    afterNextRender({
      write: () => this.enfocar(origen?.isConnected ? origen : this.nuevaPelicula()?.nativeElement),
    }, { injector: this.injector });
  }

  private enfocar(elemento: HTMLElement | undefined): void {
    elemento?.focus({ preventScroll: true });
    elemento?.scrollIntoView({ behavior: 'instant', block: 'nearest' });
  }

  actualizarBusqueda(evento: Event): void {
    this.busqueda.set((evento.target as HTMLInputElement).value);
  }

  limpiarFiltros(): void {
    this.busqueda.set('');
    this.filtroCartelera.set('todas');
    this.enfocar(this.buscador()?.nativeElement);
  }
}
