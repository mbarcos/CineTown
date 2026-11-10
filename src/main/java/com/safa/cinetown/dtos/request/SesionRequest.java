package com.safa.cinetown.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SesionRequest(
        @NotNull(message = "La película es obligatoria")
        Integer peliculaId,

        @NotNull(message = "La sala es obligatoria")
        Integer salaId,

        @NotNull(message = "La fecha y hora de inicio son obligatorias")
        LocalDateTime fechaHoraInicio,

        @NotNull(message = "El precio base es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio base no puede ser negativo")
        BigDecimal precioBase
) {
}
