import { Component, Input } from '@angular/core';
import { obtienePeliculasDto } from '../../models/dto/peliculaDTO';

@Component({
  selector: 'app-pelicula-card',
  standalone: true,
  template: `
    <article class="movie-card">
      <div class="poster" [style.background]="posterBackground" role="img" [attr.aria-label]="'Póster de ' + pelicula.titulo">
        <span class="poster-mark">CINETOWN</span>
        <div class="movie-info">
          <h3>{{ pelicula.titulo }}</h3>
          <span class="release-badge">ESTRENO · {{ pelicula.anio }}</span>
          <div class="genres">
            @for (genero of pelicula.generos; track genero) {
              <span class="genre">{{ genero }}</span>
            }
          </div>
        </div>
      </div>
    </article>
  `,
  styleUrl: './pelicula-card.scss',
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
