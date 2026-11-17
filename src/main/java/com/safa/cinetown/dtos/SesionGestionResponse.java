package com.safa.cinetown.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SesionGestionResponse(
        Integer id,
        Integer peliculaId,
        String titulo,
        Integer salaId,
        Integer numeroSala,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        BigDecimal precioBase,
        Integer capacidad,
        Integer asientosLibres
) {
}
