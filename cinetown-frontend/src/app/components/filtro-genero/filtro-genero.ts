import { Component, computed, input, model } from '@angular/core';
import { Selector } from '../selector/selector';

@Component({
  selector: 'app-filtro-genero',
  standalone: true,
  imports: [Selector],
  templateUrl: './filtro-genero.html',
  styleUrl: './filtro-genero.scss',
})
export class FiltroGeneroComponent {
  readonly generos = input<readonly string[]>([]);
  readonly generoSeleccionado = model<string | null>(null);
  readonly opciones = computed(() => [null, ...this.generos()]);
}
