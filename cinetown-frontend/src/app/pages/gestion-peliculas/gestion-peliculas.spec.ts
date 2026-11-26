import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { expect, it, vi } from 'vitest';
import { PeliculaService } from '../../services/peliculaService';
import { GestionPeliculas } from './gestion-peliculas';

it('conserva los criterios al avanzar y vuelve a la primera página al filtrar', async () => {
  const consultarPagina = vi.fn(() => of({ content: [], totalElements: 21, totalPages: 3 }));
  TestBed.configureTestingModule({
    imports: [GestionPeliculas],
    providers: [{ provide: PeliculaService, useValue: { consultarPagina, getGeneros: () => of(['Drama']) } }],
  });
  const fixture = TestBed.createComponent(GestionPeliculas);
  await fixture.whenStable();
  const vista: HTMLElement = fixture.nativeElement;
  const select = vista.querySelector<HTMLSelectElement>('.controles-catalogo select')!;
  select.value = 'Drama';
  select.dispatchEvent(new Event('change'));
  vista.querySelector<HTMLButtonElement>('.paginacion button:last-child')!.click();
  expect(consultarPagina).toHaveBeenLastCalledWith(expect.objectContaining({ page: 1, genero: 'Drama' }));

  vista.querySelector<HTMLButtonElement>('.estados-cartelera button:last-child')!.click();
  expect(consultarPagina).toHaveBeenLastCalledWith(expect.objectContaining({ page: 0, genero: 'Drama', activo: false }));
});
