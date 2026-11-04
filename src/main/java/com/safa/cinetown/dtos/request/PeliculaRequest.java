package com.safa.cinetown.dtos.request;

import com.safa.cinetown.modelos.ClasificacionEdad;
import java.util.Set;

public record PeliculaRequest(
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
