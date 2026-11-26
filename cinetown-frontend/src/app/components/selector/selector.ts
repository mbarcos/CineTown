import { ChangeDetectionStrategy, Component, ElementRef, ViewEncapsulation, inject } from '@angular/core';

@Component({
  selector: 'select[appSelector]',
  template: '<ng-content />',
  styleUrl: './selector.scss',
  host: { class: 'selector-cinetown vidrio-campo' },
  // Los estilos del desplegable también alcanzan las opciones proyectadas.
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Selector {
  private readonly elemento = inject<ElementRef<HTMLSelectElement>>(ElementRef);

  get value(): string {
    return this.elemento.nativeElement.value;
  }

  get selectedIndex(): number {
    return this.elemento.nativeElement.selectedIndex;
  }
}
