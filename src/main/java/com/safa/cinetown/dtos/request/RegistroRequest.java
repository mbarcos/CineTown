package com.safa.cinetown.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank
        @Size(max = 50)
        String nombreUsuario,

        @NotBlank
        @Size(min = 8)
        String password
)
{}
