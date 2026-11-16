package com.safa.cinetown.conversores;

import com.safa.cinetown.dtos.PeliculaResponse;
import com.safa.cinetown.dtos.request.PeliculaRequest;
import com.safa.cinetown.modelos.Genero;
import com.safa.cinetown.modelos.Pelicula;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PeliculaMapper {
    PeliculaResponse toResponse(Pelicula pelicula);

    List<PeliculaResponse> toResponseList(List<Pelicula> peliculas);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "generos", ignore = true)
    Pelicula aEntidad(PeliculaRequest dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "generos", ignore = true)
    void actualizarEntidad(PeliculaRequest dto, @MappingTarget Pelicula pelicula);

    @AfterMapping
    default void actualizarActivo(PeliculaRequest dto, @MappingTarget Pelicula pelicula) {
        if (dto.activo() != null) {
            pelicula.setActivo(dto.activo());
        }
    }

    default String map(Genero genero) {
        return genero != null ? genero.getNombre() : null;
    }
}
