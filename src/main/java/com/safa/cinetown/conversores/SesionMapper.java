package com.safa.cinetown.conversores;

import com.safa.cinetown.dtos.request.SesionRequest;
import com.safa.cinetown.modelos.Pelicula;
import com.safa.cinetown.modelos.Sala;
import com.safa.cinetown.modelos.Sesion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface SesionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pelicula", source = "pelicula")
    @Mapping(target = "sala", source = "sala")
    @Mapping(target = "fechaHoraInicio", source = "request.fechaHoraInicio")
    @Mapping(target = "fechaHoraFin", source = "fin")
    @Mapping(target = "precioBase", source = "request.precioBase")
    Sesion aEntidad(SesionRequest request, Pelicula pelicula, Sala sala, LocalDateTime fin);
}
