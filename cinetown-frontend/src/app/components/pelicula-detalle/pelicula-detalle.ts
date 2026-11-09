import {ActivatedRoute} from '@angular/router';
import {Component, inject} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {map, switchMap} from 'rxjs';
import {PeliculaService} from '../../services/peliculaService';
import { Clasificacion, SesionPelicula } from '../../models/pelicula-detalle.model';
import {CurrencyPipe, DatePipe} from '@angular/common';

@Component({
  selector: 'app-pelicula-detalle',
  templateUrl: './pelicula-detalle.html',
  styleUrl: './pelicula-detalle.scss',
  imports: [
    CurrencyPipe,
    DatePipe
  ]
})
export class PeliculaDetalle {
  private readonly etiquetas: Record<Clasificacion, string> = {
    TP: 'Todos los públicos',
    P7: '+7',
    P12: '+12',
    P16: '+16',
    P18: '+18',
  };
  private readonly route = inject(ActivatedRoute);
  private readonly peliculaService = inject(PeliculaService);

  readonly pelicula = toSignal(
    this.route.paramMap.pipe(
      map(params => Number(params.get('id'))),
      switchMap(id => this.peliculaService.obtenerDetalle(id)),
    ),
  );

  etiquetaClasificacion(clasificacion: Clasificacion): string {
    return this.etiquetas[clasificacion];
  }

  sesionesFuturas(sesiones: SesionPelicula[]): SesionPelicula[] {
    const ahora = Date.now();
    return sesiones.filter(s => new Date(s.fechaHoraInicio).getTime() > ahora);
  }
}
