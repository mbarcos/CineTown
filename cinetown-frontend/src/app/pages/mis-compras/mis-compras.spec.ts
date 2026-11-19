import { TestBed } from '@angular/core/testing';
import { of, Subject } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { AuthService } from '../../services/authService';
import { CompraResumen, CompraService } from '../../services/compraService';
import { MisCompras } from './mis-compras';

describe('Mis compras', () => {
  it('conserva la compra al cerrar la confirmación y solo cancela tras confirmarla', async () => {
    const compra: CompraResumen = {
      codCompra: 'PRUEBA42', titulo: 'La llegada', numeroSala: 2, numEntradas: 1, precioTotal: 8.5,
      fechaHoraInicio: '2099-11-19T19:00:00', fechaCompra: '2099-11-16T10:00:00', estado: true,
    };
    const respuesta = new Subject<void>();
    const cancelarCompra = vi.fn(() => respuesta);
    const consultarCompras = vi.fn()
      .mockReturnValueOnce(of({ content: [compra], totalPages: 1, totalElements: 1 }))
      .mockReturnValueOnce(of({ content: [{ ...compra, estado: false }], totalPages: 1, totalElements: 1 }));

    TestBed.configureTestingModule({
      imports: [MisCompras],
      providers: [
        { provide: AuthService, useValue: { tieneAlgunRol: (roles: string[]) => roles.includes('CLIENTE') } },
        { provide: CompraService, useValue: { consultarCompras, cancelarCompra } },
      ],
    });

    const fixture = TestBed.createComponent(MisCompras);
    const pagina: HTMLElement = fixture.nativeElement;
    await fixture.whenStable();
    const origen = pagina.querySelector<HTMLButtonElement>('.accion-cancelar')!;
    origen.click();
    await fixture.whenStable();

    const conservar = pagina.querySelector<HTMLButtonElement>('.confirmacion button')!;
    expect(document.activeElement).toBe(conservar);
    conservar.click();
    await fixture.whenStable();
    expect(pagina.querySelector('.confirmacion')).toBeNull();
    expect(document.activeElement).toBe(origen);
    expect(cancelarCompra).not.toHaveBeenCalled();

    origen.click();
    await fixture.whenStable();
    const confirmar = pagina.querySelector<HTMLButtonElement>('.accion-peligro')!;
    confirmar.click();
    confirmar.click();
    expect(cancelarCompra).toHaveBeenCalledExactlyOnceWith('PRUEBA42');

    respuesta.next();
    await fixture.whenStable();
    expect(consultarCompras).toHaveBeenCalledTimes(2);
    expect(pagina.querySelector('.estado-compra')?.textContent?.trim()).toBe('Cancelada');
    expect(document.activeElement).toBe(pagina.querySelector('[data-compra="PRUEBA42"]'));
  });
});
