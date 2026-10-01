package com.safa.cinetown.proyecciones;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SesionConAsientos {
    Integer getId();

    String getTitulo();

    Integer getNumeroSala();

    LocalDateTime getFechaHoraInicio();

    LocalDateTime getFechaHoraFin();

    BigDecimal getPrecioBase();

    Integer getCapacidad();

    Long getAsientosLibres();
}
