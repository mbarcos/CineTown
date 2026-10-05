package com.safa.cinetown.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SesionConAsientos(
        Integer id,
        String titulo,
        Integer numeroSala,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        BigDecimal precioBase,
        Integer capacidad,
        Long asientosLibres
) {
}
