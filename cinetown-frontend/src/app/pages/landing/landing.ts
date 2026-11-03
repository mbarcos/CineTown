import { Component, signal, OnInit, inject } from '@angular/core';
import { obtienePeliculasDto } from '../../models/dto/peliculaDTO';
import { PeliculaService } from '../../services/peliculaService';
import { TitularComponent } from '../../components/titular/titular';
import { PeliculaCardComponent } from '../../components/pelicula-card/pelicula-card';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [TitularComponent, PeliculaCardComponent],
  styleUrl: './landing.scss',
  templateUrl: './landing.html',
})
export class Landing implements OnInit {
  private peliculaService = inject(PeliculaService);
  peliculas = signal<obtienePeliculasDto[]>([]);

  ngOnInit(): void {
    this.peliculaService.getPeliculas().subscribe({
      next: (data) => this.peliculas.set(data),
      error: (error) => console.error('Error al obtener las películas:', error),
    });
  }
}
