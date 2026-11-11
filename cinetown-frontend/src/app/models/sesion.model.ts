export interface SesionConAsientos {
  id: number;
  titulo: string;
  numeroSala: number;
  fechaHoraInicio: string;
  fechaHoraFin: string;
  precioBase: number;
  capacidad: number;
  asientosLibres: number;
}
