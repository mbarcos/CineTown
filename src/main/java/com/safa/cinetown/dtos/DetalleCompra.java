package com.safa.cinetown.dtos;

import java.time.LocalDateTime;

public record DetalleCompra(
        String codCompra,
        Integer numeroSala,
        LocalDateTime fechaHoraInicio,
        String titulo
) {}
