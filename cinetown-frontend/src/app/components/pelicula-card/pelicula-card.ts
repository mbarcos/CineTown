import { Component, Input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { obtienePeliculasDto } from '../../models/dto/peliculaDTO';
import { obtenerFondoPoster } from '../../shared/utils/pelicula-poster';

@Component({
  selector: 'app-pelicula-card',
  standalone: true,
  templateUrl: './pelicula-card.html',
  styleUrl: './pelicula-card.scss',
  imports: [
    RouterLink
  ]
})
export class PeliculaCardComponent {
  @Input({ required: true }) pelicula!: obtienePeliculasDto;

  protected readonly cartelNoDisponible = signal<string | null>(null);

  protected get fondoCartel(): string {
    return obtenerFondoPoster(this.pelicula.id);
  }
}
