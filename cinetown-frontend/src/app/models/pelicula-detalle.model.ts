export type Clasificacion = 'TP' | 'P7' | 'P12' | 'P16' | 'P18';

export interface SesionPelicula {
  id: number;
  fechaHoraInicio: string;
  precioBase: number;
}

export interface PeliculaDetalle {
  id: number;
  titulo: string;
  sinopsis: string | null;
  duracion: number;
  anio: number;
  clasificacion: Clasificacion;
  activo: boolean;
  generos: string[];
  sesiones: SesionPelicula[];
}
