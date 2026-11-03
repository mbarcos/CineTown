package com.safa.cinetown.conversores;

import com.safa.cinetown.dtos.PeliculaResponse;
import com.safa.cinetown.dtos.PeliculaDetalleResponse;
import com.safa.cinetown.dtos.SesionPeliculaResponse;
import com.safa.cinetown.modelos.Genero;
import com.safa.cinetown.modelos.Pelicula;
import com.safa.cinetown.modelos.Sesion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PeliculaMapper {
    @Mapping(target = "generos", source = "generos")
    PeliculaResponse toResponse(Pelicula pelicula);

    @Mapping(target = "id", source = "pelicula.id")
    @Mapping(target = "titulo", source = "pelicula.titulo")
    @Mapping(target = "sinopsis", source = "pelicula.sinopsis")
    @Mapping(target = "duracion", source = "pelicula.duracion")
    @Mapping(target = "anio", source = "pelicula.anio")
    @Mapping(target = "clasificacion", source = "pelicula.clasificacion")
    @Mapping(target = "activo", source = "pelicula.activo")
    @Mapping(target = "generos", source = "pelicula.generos")
    @Mapping(target = "sesiones", source = "sesiones")
    PeliculaDetalleResponse toDetalleResponse(Pelicula pelicula, List<Sesion> sesiones);

    SesionPeliculaResponse toSesionResponse(Sesion sesion);

    List<PeliculaResponse> toResponseList(List<Pelicula> peliculas);

    List<SesionPeliculaResponse> toSesionResponseList(List<Sesion> sesiones);

    default String map(Genero genero) {
        return genero.getNombre();
    }
}
