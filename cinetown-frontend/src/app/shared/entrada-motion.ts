import { afterNextRender, DestroyRef, Directive, ElementRef, inject } from '@angular/core';
import { animate } from 'motion/mini';

@Directive({
  selector: '[appEntradaMotion]',
})
export class EntradaMotion {
  private readonly elemento = inject(ElementRef<HTMLElement>).nativeElement;
  private readonly destroyRef = inject(DestroyRef);

  constructor() {
    afterNextRender(() => {
      if (typeof this.elemento.animate !== 'function') return;

      const movimientoReducido = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;
      const animacion = animate(
        this.elemento,
        movimientoReducido
          ? { opacity: [0.8, 1] }
          : { opacity: [0.8, 1], translate: ['0 6px', 'none'] },
        {
          duration: movimientoReducido ? 0.12 : 0.22,
          ease: [0.22, 1, 0.36, 1],
        },
      );

      this.destroyRef.onDestroy(() => animacion.cancel());
    });
  }
}
