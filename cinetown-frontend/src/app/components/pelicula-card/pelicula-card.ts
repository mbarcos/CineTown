import { Component, Input } from '@angular/core';
import { obtienePeliculasDto } from '../../models/dto/peliculaDTO';
import {RouterLink} from '@angular/router';

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

  private readonly posterColors = [
    ['#29334f', '#171e32'],
    ['#713632', '#351e24'],
    ['#28504c', '#142c2c'],
    ['#53364f', '#2b1b2c'],
    ['#39446c', '#202944'],
  ];

  get posterBackground(): string {
    const [start, end] = this.posterColors[Math.abs(this.pelicula?.id ?? 0) % this.posterColors.length];
    return `linear-gradient(145deg, ${start}, ${end})`;
  }
}
