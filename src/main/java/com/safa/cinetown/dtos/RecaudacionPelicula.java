package com.safa.cinetown.dtos;

import java.math.BigDecimal;

public record RecaudacionPelicula(
        Integer peliculaId,
        String titulo,
        Long entradasVendidas,
        BigDecimal recaudacionTotal
) {}
