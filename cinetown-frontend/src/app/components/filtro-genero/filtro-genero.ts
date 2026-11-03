import { NgClass } from '@angular/common';
import { Component, computed, input, model } from '@angular/core';

@Component({
  selector: 'app-filtro-genero',
  standalone: true,
  imports: [NgClass],
  templateUrl: './filtro-genero.html',
  styleUrl: './filtro-genero.scss',
})
export class FiltroGeneroComponent {
  readonly generos = input<readonly string[]>([]);
  readonly generoSeleccionado = model<string | null>(null);
  readonly opciones = computed(() => [null, ...this.generos()]);
}
