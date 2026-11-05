package com.safa.cinetown.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.safa.cinetown.modelos.ClasificacionEdad;
import jakarta.validation.constraints.*;

import java.util.Set;

public record PeliculaRequest(
        Integer id,

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 255, message = "El título no puede superar los 255 caracteres")
        String titulo,

        String sinopsis,

        @NotNull(message = "La duración es obligatoria")
        @Min(value = 1, message = "La duración mínima es de 1 minuto")
        @Max(value = 300, message = "La duración máxima es de 300 minutos")
        Integer duracion,

        @NotNull(message = "El año es obligatorio")
        Integer anio,

        @NotNull(message = "La clasificación es obligatoria")
        ClasificacionEdad clasificacion,

        @JsonProperty(defaultValue = "false")
        Boolean activo,

        @NotEmpty(message = "La película debe tener al menos un género")
        Set<String> generos
) {
}
