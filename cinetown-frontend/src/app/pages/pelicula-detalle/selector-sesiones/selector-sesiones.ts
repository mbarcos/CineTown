import { Component, input, linkedSignal } from '@angular/core';
import { RouterLink } from '@angular/router';

export interface DiaConSesiones {
  fecha: string;
  etiqueta: string;
  sesiones: {
    id: number;
    fechaHoraInicio: string;
    horaLegible: string;
    precioLegible: string;
  }[];
}

@Component({
  selector: 'app-selector-sesiones',
  imports: [RouterLink],
  templateUrl: './selector-sesiones.html',
  styleUrl: './selector-sesiones.scss',
})
export class SelectorSesiones {
  readonly dias = input.required<DiaConSesiones[]>();
  readonly diaSeleccionado = linkedSignal(() => this.dias()[0]);

  private readonly formatoDia = new Intl.DateTimeFormat('es-ES', {
    weekday: 'short',
    timeZone: 'UTC',
  });
  private readonly formatoFecha = new Intl.DateTimeFormat('es-ES', {
    day: 'numeric',
    month: 'short',
    timeZone: 'UTC',
  });

  diaCorto(fecha: string): string {
    return this.formatoDia.format(new Date(fecha));
  }

  fechaCorta(fecha: string): string {
    return this.formatoFecha.format(new Date(fecha));
  }
}
