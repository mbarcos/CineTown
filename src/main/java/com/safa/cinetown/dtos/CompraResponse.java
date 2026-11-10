package com.safa.cinetown.dtos;

import java.math.BigDecimal;

public record CompraResponse(
        String codCompra,
        Integer numEntradas,
        BigDecimal precioTotal
) {
}
