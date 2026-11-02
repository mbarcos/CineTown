package com.safa.cinetown.conversores;

import com.safa.cinetown.dtos.PeliculaResponse;
import com.safa.cinetown.modelos.Genero;
import com.safa.cinetown.modelos.Pelicula;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PeliculaMapper {
    @Mapping(target = "generos", source = "generos")
    PeliculaResponse toResponse(Pelicula pelicula);

    List<PeliculaResponse> toResponseList(List<Pelicula> peliculas);

    default String map(Genero genero) {
        return genero.getNombre();
    }
}
