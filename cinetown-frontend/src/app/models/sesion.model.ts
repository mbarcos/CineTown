export interface SesionConAsientos {
  id: number;
  titulo: string;
  numeroSala: number;
  fechaHoraInicio: string;
  fechaHoraFin: string;
  precioBase: number;
  precioEntrada?: number;
  capacidad: number;
  asientosLibres: number;
}

export interface SesionGestion {
  id: number;
  peliculaId: number;
  titulo: string;
  salaId: number;
  numeroSala: number;
  fechaHoraInicio: string;
  fechaHoraFin: string;
  precioBase: number;
  capacidad: number;
  asientosLibres: number;
}
