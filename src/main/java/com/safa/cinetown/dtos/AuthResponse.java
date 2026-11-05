package com.safa.cinetown.dtos;

import java.util.List;

public record AuthResponse (
        String token,
        String tokenType,
        String nombreUsuario,
        List<String> roles
){}
