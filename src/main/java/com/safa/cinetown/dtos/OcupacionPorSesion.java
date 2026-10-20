package com.safa.cinetown.dtos;

import java.time.LocalDateTime;

public record OcupacionPorSesion(
        Integer sesionId,
        String titulo,
        Integer numeroSala,
        LocalDateTime fechaHoraInicio,
        Integer capacidad,
        Integer entradasVendidas,
        Double porcentajeOcupacion
) {
    public OcupacionPorSesion(Integer sesionId, String titulo, Integer numeroSala,
                           LocalDateTime fechaHoraInicio, Integer capacidad, Integer entradasVendidas) {
        this(sesionId, titulo, numeroSala, fechaHoraInicio, capacidad, entradasVendidas,
                calcularPorcentaje(entradasVendidas, capacidad));
    }

    private static Double calcularPorcentaje(Integer entradasVendidas, Integer capacidad) {
        if (capacidad == null || capacidad == 0) {
            return 0.0;
        }
        return Math.round(entradasVendidas * 10000.0 / capacidad) / 100.0;
    }
}
