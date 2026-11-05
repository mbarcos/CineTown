package com.safa.cinetown.conversores;

import com.safa.cinetown.dtos.PeliculaResponse;
import com.safa.cinetown.dtos.PeliculaDetalleResponse;
import com.safa.cinetown.dtos.SesionPeliculaResponse;
import com.safa.cinetown.dtos.request.PeliculaRequest;
import com.safa.cinetown.modelos.Genero;
import com.safa.cinetown.modelos.Pelicula;
import com.safa.cinetown.modelos.Sesion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PeliculaMapper {
    PeliculaResponse toResponse(Pelicula pelicula);

    PeliculaDetalleResponse toDetalleResponse(Pelicula pelicula, List<Sesion> sesiones);

    SesionPeliculaResponse toSesionResponse(Sesion sesion);

    List<PeliculaResponse> toResponseList(List<Pelicula> peliculas);

    List<SesionPeliculaResponse> toSesionResponseList(List<Sesion> sesiones);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "generos", ignore = true)
    Pelicula aEntidad(PeliculaRequest dto);

    default String map(Genero genero) {
        return genero != null ? genero.getNombre() : null;
    }
}
