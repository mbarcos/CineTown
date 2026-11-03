package com.safa.cinetown.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SesionPeliculaResponse(
        Integer id,
        LocalDateTime fechaHoraInicio,
        BigDecimal precioBase
) {
}
