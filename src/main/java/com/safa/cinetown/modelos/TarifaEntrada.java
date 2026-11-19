package com.safa.cinetown.modelos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

public final class TarifaEntrada {
    private static final BigDecimal MULTIPLICADOR_MIERCOLES = new BigDecimal("0.70");

    private TarifaEntrada() {
    }

    public static BigDecimal calcular(BigDecimal precioBase, LocalDateTime fechaHoraInicio) {
        if (fechaHoraInicio.getDayOfWeek() == DayOfWeek.WEDNESDAY) {
            return precioBase.multiply(MULTIPLICADOR_MIERCOLES).setScale(2, RoundingMode.HALF_UP);
        }
        return precioBase;
    }
}
