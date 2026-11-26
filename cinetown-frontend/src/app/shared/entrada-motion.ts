import { afterNextRender, DestroyRef, Directive, ElementRef, inject, input } from '@angular/core';
import { animate } from 'motion/mini';

const entradasPendientes = new WeakMap<Element, () => void>();
let observadorEntradas: IntersectionObserver | undefined;

function observarEntrada(elemento: HTMLElement, iniciar: () => void): () => void {
  const observador = (observadorEntradas ??= new IntersectionObserver((entradas, observer) => {
    for (const entrada of entradas) {
      if (!entrada.isIntersecting) continue;

      const animar = entradasPendientes.get(entrada.target);
      entradasPendientes.delete(entrada.target);
      observer.unobserve(entrada.target);
      animar?.();
    }
  }));

  entradasPendientes.set(elemento, iniciar);
  observador.observe(elemento);

  return () => {
    entradasPendientes.delete(elemento);
    observador.unobserve(elemento);
  };
}

@Directive({
  selector: '[appEntradaMotion]',
})
export class EntradaMotion {
  readonly ordenEntrada = input<number>();
  private readonly elemento = inject(ElementRef<HTMLElement>).nativeElement;
  private readonly destroyRef = inject(DestroyRef);

  constructor() {
    afterNextRender(() => {
      if (typeof this.elemento.animate !== 'function') return;

      if (this.ordenEntrada() !== undefined && typeof IntersectionObserver === 'function') {
        const dejarDeObservar = observarEntrada(this.elemento, () => this.animarEntrada());
        this.destroyRef.onDestroy(dejarDeObservar);
        return;
      }

      this.animarEntrada();
    });
  }

  private animarEntrada(): void {
    const movimientoReducido = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;
    const orden = this.ordenEntrada();
    const escalonada = orden !== undefined && !movimientoReducido;
    const animacion = animate(
      this.elemento,
      movimientoReducido
        ? { opacity: [0.8, 1] }
        : {
            opacity: [escalonada ? 0.45 : 0.8, 1],
            translate: [escalonada ? '0 14px' : '0 6px', 'none'],
          },
      {
        duration: movimientoReducido ? 0.12 : 0.22,
        delay: escalonada ? Math.min(orden, 2) * 0.03 : 0,
        ease: [0.22, 1, 0.36, 1],
      },
    );

    this.destroyRef.onDestroy(() => animacion.cancel());
  }
}
