import { ClasificacionEdad } from '../enums';

export interface obtienePeliculasDto {
  id: number,
  titulo: string
  sinopsis: string,
  duracion: number,
  anio: number,
  clasificacion: ClasificacionEdad,
  activo: boolean,
  generos: Set<string>
}
