import { TestBed } from '@angular/core/testing';
import { of, Subject } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { PeliculaService } from '../../services/peliculaService';
import { SesionService } from '../../services/sesionService';
import { GestionSesiones } from './gestion-sesiones';

describe('Gestión de sesiones', () => {
  it.each([false, true])('guarda una sesión una sola vez (edición: %s)', async (editar) => {
    const respuesta = new Subject<void>();
    const programarSesion = vi.fn(() => respuesta);
    const modificarSesion = vi.fn(() => respuesta);
    const agenda = vi.fn(() => of([]));

    TestBed.configureTestingModule({
      imports: [GestionSesiones],
      providers: [
        { provide: PeliculaService, useValue: {
          getPeliculasParaGestion: () => of([{ id: 7, titulo: 'La llegada', activo: true }]),
        } },
        { provide: SesionService, useValue: {
          consultarSalas: () => of([{ id: 3, numero: 2, capacidad: 80 }]),
          consultarSesionesParaGestion: agenda,
          programarSesion,
          modificarSesion,
        } },
      ],
    });

    const fixture = TestBed.createComponent(GestionSesiones);
    const pagina: HTMLElement = fixture.nativeElement;
    await fixture.whenStable();
    const fecha = pagina.querySelector<HTMLInputElement>('#sesion-fecha')!.value;

    if (editar) {
      fixture.componentInstance.sesiones.set([{
        id: 42, peliculaId: 7, titulo: 'La llegada', salaId: 3, numeroSala: 2,
        fechaHoraInicio: `${fecha}T19:00:00`, fechaHoraFin: `${fecha}T21:00:00`,
        precioBase: 8, capacidad: 80, asientosLibres: 79,
      }]);
      fixture.componentInstance.fechaListado.set(fecha);
      await fixture.whenStable();
      pagina.querySelector<HTMLButtonElement>('.boton-editar')!.click();
      await fixture.whenStable();
    }

    const guardar = pagina.querySelector<HTMLButtonElement>('button[type="submit"]')!;
    guardar.click();
    guardar.click();

    const datos = {
      peliculaId: 7, salaId: 3, fechaHoraInicio: `${fecha}T${editar ? '19:00' : '16:00'}:00`,
      precioBase: editar ? 8 : 8.9,
    };
    if (editar) {
      expect(modificarSesion).toHaveBeenCalledExactlyOnceWith(42, datos);
      expect(programarSesion).not.toHaveBeenCalled();
    } else {
      expect(programarSesion).toHaveBeenCalledExactlyOnceWith(datos);
      expect(modificarSesion).not.toHaveBeenCalled();
    }

    respuesta.next();
    await fixture.whenStable();
    expect(agenda).toHaveBeenCalledTimes(2);
    expect(pagina.querySelector('.aviso[role="status"]')?.textContent?.trim())
      .toBe(editar ? 'Cambios guardados.' : 'Sesión programada.');
    expect(pagina.querySelector<HTMLInputElement>('#agenda-fecha')?.value).toBe(fecha);
  });
});
