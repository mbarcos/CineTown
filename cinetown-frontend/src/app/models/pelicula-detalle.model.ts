export type Clasificacion = 'TP' | 'P7' | 'P12' | 'P16' | 'P18';

export interface PeliculaResponse {
  id: number;
  titulo: string;
  sinopsis: string | null;
  duracion: number;
  anio: number;
  clasificacion: Clasificacion;
  activo: boolean;
  generos: string[];
}
