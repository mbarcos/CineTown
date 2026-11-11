package com.safa.cinetown.dtos;

import com.safa.cinetown.modelos.ClasificacionEdad;

import java.util.Set;

public record PeliculaDetalleResponse(
        Integer id,
        String titulo,
        String sinopsis,
        Integer duracion,
        Integer anio,
        ClasificacionEdad clasificacion,
        Boolean activo,
        Set<String> generos
) {
}
