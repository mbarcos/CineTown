package com.safa.cinetown.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CompraResumen(
        String codCompra,
        String titulo,
        Integer numeroSala,
        LocalDateTime fechaHoraInicio,
        Integer numEntradas,
        BigDecimal precioTotal,
        Boolean estado,
        LocalDateTime fechaCompra
) {}