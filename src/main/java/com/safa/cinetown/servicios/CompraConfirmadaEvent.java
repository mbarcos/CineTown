package com.safa.cinetown.servicios;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CompraConfirmadaEvent(
        String codigo,
        String email,
        String pelicula,
        LocalDateTime fechaSesion,
        Integer sala,
        Integer entradas,
        BigDecimal total
) {
}
