package com.safa.cinetown.conversores;

import com.safa.cinetown.dtos.PeliculaResponse;
import com.safa.cinetown.dtos.PeliculaDetalleResponse;
import com.safa.cinetown.dtos.SesionPeliculaResponse;
import com.safa.cinetown.modelos.Genero;
import com.safa.cinetown.modelos.Pelicula;
import com.safa.cinetown.modelos.Sesion;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PeliculaMapper {
    PeliculaResponse toResponse(Pelicula pelicula);

    PeliculaDetalleResponse toDetalleResponse(Pelicula pelicula, List<Sesion> sesiones);

    SesionPeliculaResponse toSesionResponse(Sesion sesion);

    List<PeliculaResponse> toResponseList(List<Pelicula> peliculas);

    List<SesionPeliculaResponse> toSesionResponseList(List<Sesion> sesiones);

    default String map(Genero genero) {
        return genero != null ? genero.getNombre() : null;
    }
}
