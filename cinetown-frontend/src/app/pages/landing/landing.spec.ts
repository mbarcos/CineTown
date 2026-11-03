import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { obtienePeliculasDto } from '../../models/dto/peliculaDTO';
import { ClasificacionEdad } from '../../models/enums';
import { PeliculaService } from '../../services/peliculaService';
import { Landing } from './landing';

describe('Landing', () => {
  let component: Landing;
  let fixture: ComponentFixture<Landing>;
  const peliculas: obtienePeliculasDto[] = [
    {
      id: 1,
      titulo: 'Película de acción',
      sinopsis: '',
      duracion: 100,
      anio: 2025,
      clasificacion: ClasificacionEdad.TP,
      activo: true,
      generos: ['Acción', 'Drama'],
    },
    {
      id: 2,
      titulo: 'Película de comedia',
      sinopsis: '',
      duracion: 90,
      anio: 2025,
      clasificacion: ClasificacionEdad.TP,
      activo: true,
      generos: ['Comedia'],
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Landing],
      providers: [
        {
          provide: PeliculaService,
          useValue: { getPeliculas: () => of(peliculas) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Landing);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('filters movies by genre and returns to all movies', () => {
    expect(component.generosDisponibles()).toEqual(['Acción', 'Comedia', 'Drama']);
    expect(fixture.nativeElement.querySelectorAll('app-pelicula-card')).toHaveLength(2);

    const actionButton = Array.from(
      (fixture.nativeElement as HTMLElement).querySelectorAll('button'),
    ).find((button) => button.textContent?.trim() === 'Acción');
    actionButton?.click();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('app-pelicula-card')).toHaveLength(1);
    expect(fixture.nativeElement.textContent).toContain('Película de acción');

    const allButton = Array.from(
      (fixture.nativeElement as HTMLElement).querySelectorAll('button'),
    ).find((button) => button.textContent?.trim() === 'Todas');
    allButton?.click();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('app-pelicula-card')).toHaveLength(2);
  });

  it('distinguishes a failed request from an empty catalog', () => {
    component.estadoCarga.set('error');
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('No se pudo cargar la cartelera');
  });
});
