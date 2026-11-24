import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, input, linkedSignal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { obtienePeliculasDto } from '../../models/dto/peliculaDTO';
import { obtenerFondoPoster } from '../../shared/utils/pelicula-poster';

@Component({
  selector: 'app-pelicula-card',
  standalone: true,
  templateUrl: './pelicula-card.html',
  styleUrl: './pelicula-card.scss',
  imports: [
    RouterLink, NgTemplateOutlet
  ]
})
export class PeliculaCardComponent {
  readonly pelicula = input.required<Pick<obtienePeliculasDto, 'id' | 'titulo' | 'generos' | 'cartelUrl'> & { anio: number | null }>();
  readonly vistaPrevia = input(false);

  protected readonly fondoCartel = computed(() => obtenerFondoPoster(this.pelicula().id));
  private readonly urlImagen = computed(() => this.pelicula().cartelUrl);
  protected readonly imagenes = computed(() => {
    const url = this.urlImagen();
    return url ? [url] : [];
  });
  protected readonly estadoImagen = linkedSignal((): 'cargando' | 'lista' | 'error' => {
    this.urlImagen();
    return 'cargando';
  });

  protected actualizarImagen(url: string, estado: 'lista' | 'error'): void {
    if (url === this.urlImagen()) this.estadoImagen.set(estado);
  }
}
