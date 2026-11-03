import { Component, computed, signal, OnInit, inject } from '@angular/core';
import { obtienePeliculasDto } from '../../models/dto/peliculaDTO';
import { PeliculaService } from '../../services/peliculaService';
import { TitularComponent } from '../../components/titular/titular';
import { PeliculaCardComponent } from '../../components/pelicula-card/pelicula-card';
import { FiltroGeneroComponent } from '../../components/filtro-genero/filtro-genero';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [TitularComponent, PeliculaCardComponent, FiltroGeneroComponent],
  templateUrl: './landing.html',
})
export class Landing implements OnInit {
  private peliculaService = inject(PeliculaService);
  peliculas = signal<obtienePeliculasDto[]>([]);
  estadoCarga = signal<'cargando' | 'error' | 'listo'>('cargando');
  generoSeleccionado = signal<string | null>(null);
  generosDisponibles = computed(() =>
    [...new Set(this.peliculas().flatMap((pelicula) => pelicula.generos))].sort((a, b) =>
      a.localeCompare(b, 'es'),
    ),
  );
  peliculasFiltradas = computed(() => {
    const generoSeleccionado = this.generoSeleccionado();
    return generoSeleccionado
      ? this.peliculas().filter((pelicula) => pelicula.generos.includes(generoSeleccionado))
      : this.peliculas();
  });

  ngOnInit(): void {
    this.peliculaService.getPeliculas().subscribe({
      next: (data) => {
        this.peliculas.set(data);
        this.estadoCarga.set('listo');
      },
      error: (error) => {
        console.error('Error al obtener las películas:', error);
        this.estadoCarga.set('error');
      },
    });
  }
}
