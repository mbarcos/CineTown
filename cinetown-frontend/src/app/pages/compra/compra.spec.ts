import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of, Subject } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { CompraResponse, CompraService } from '../../services/compraService';
import { SesionService } from '../../services/sesionService';
import { Compra } from './compra';

describe('Compra', () => {
  it('confirma una sola vez y muestra el código con el total de la tarifa de la sesión', async () => {
    const respuesta = new Subject<CompraResponse>();
    const comprarEntradas = vi.fn(() => respuesta);
    TestBed.configureTestingModule({
      imports: [Compra],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ id: '42' })) } },
        { provide: CompraService, useValue: { comprarEntradas } },
        {
          provide: SesionService,
          useValue: {
            consultarSesionPorId: () => of({
              id: 42,
              titulo: 'El viaje de Chihiro',
              numeroSala: 4,
              fechaHoraInicio: '2026-11-25T19:00:00',
              precioBase: 10,
              precioEntrada: 7,
              asientosLibres: 2,
            }),
          },
        },
      ],
    });

    const fixture = TestBed.createComponent(Compra);
    const pagina: HTMLElement = fixture.nativeElement;
    fixture.detectChanges();

    pagina.querySelector<HTMLButtonElement>('button[aria-label="Añadir una entrada"]')!.click();
    const correo = pagina.querySelector<HTMLInputElement>('#correo-compra')!;
    correo.value = 'prueba@example.com';
    correo.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();

    expect(pagina.querySelector('.total dd')?.textContent).toMatch(/14,00\s€/);

    const formulario = pagina.querySelector('form')!;
    formulario.dispatchEvent(new Event('submit', { cancelable: true }));
    formulario.dispatchEvent(new Event('submit', { cancelable: true }));
    expect(comprarEntradas).toHaveBeenCalledExactlyOnceWith({
      sesionId: 42,
      numEntradas: 2,
      email: 'prueba@example.com',
    });

    respuesta.next({ codCompra: 'PRUEBA42', numEntradas: 2, precioTotal: 14 });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(pagina.querySelector('h1')?.textContent).toBe('Compra confirmada');
    expect(pagina.querySelector('.codigo dd')?.textContent).toBe('PRUEBA42');
    expect(pagina.querySelector('.confirmacion')?.textContent).toContain('prueba@example.com');
  });
});
