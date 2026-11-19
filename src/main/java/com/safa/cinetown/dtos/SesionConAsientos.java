package com.safa.cinetown.dtos;

import com.safa.cinetown.modelos.TarifaEntrada;

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
        Integer asientosLibres
) {
    public BigDecimal getPrecioEntrada() {
        return TarifaEntrada.calcular(precioBase, fechaHoraInicio);
    }
}
