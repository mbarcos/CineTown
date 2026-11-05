package com.safa.cinetown.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank
        String nombreUsuario,

        @NotBlank
        String password
) {}
