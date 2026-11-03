import { Component, signal, OnInit, inject } from '@angular/core';
import { obtienePeliculasDto } from '../../models/dto/peliculaDTO';
import { PeliculaService } from '../../services/peliculaService';

@Component({
  imports: [],
  selector: 'app-landing',
  styleUrl: './landing.scss',
  templateUrl: './landing.html',
})
export class Landing implements OnInit {

  private peliculaService = inject(PeliculaService);

  peliculas = signal<obtienePeliculasDto[]>([]);

  ngOnInit(): void {
    this.peliculaService.getPeliculas().subscribe({
      next: (data) => {
        this.peliculas.set(data);
        console.log('Películas obtenidas:', data);
      },
      error: (error) => {
        console.error('Error al obtener las películas:', error);
      }
    });
  }
}
