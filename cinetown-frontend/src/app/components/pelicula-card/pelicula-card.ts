import { Component, Input } from '@angular/core';
import { obtienePeliculasDto } from '../../models/dto/peliculaDTO';
import {RouterLink} from '@angular/router';
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

  get posterBackground(): string {
    return obtenerFondoPoster(this.pelicula?.id ?? 0);
  }
}
