package com.safa.cinetown.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompraRequest(
        @NotNull(message = "La sesión es obligatoria")
        Integer sesionId,

        @NotNull(message = "El número de entradas es obligatorio")
        @Min(value = 1, message = "La compra debe incluir al menos una entrada")
        @Max(value = 10, message = "La compra no puede superar las 10 entradas")
        Integer numEntradas,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        String email
) {
}
