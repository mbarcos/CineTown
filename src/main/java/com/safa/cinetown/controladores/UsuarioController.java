package com.safa.cinetown.controladores;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UsuarioController {

    @GetMapping("/admin/prueba")
    public Map<String, String> admin() {

        return Map.of(
                "mensaje",
                "Bienvenido al panel de administración"
        );
    }

    @GetMapping("/taquilla/prueba")
    public Map<String, String> taquilla() {

        return Map.of(
                "mensaje",
                "Bienvenido al sistema de taquilla"
        );
    }

    @GetMapping("/cliente/prueba")
    public Map<String, String> cliente(
            Authentication authentication) {

        return Map.of(
                "mensaje",
                "Bienvenido a CineTown",
                "usuario",
                authentication.getName()
        );
    }
}
