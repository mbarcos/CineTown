package com.safa.cinetown.controladores;

import com.safa.cinetown.dtos.CuentaPeliculasPorGenero;
import com.safa.cinetown.servicios.GeneroService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/generos")
@AllArgsConstructor
public class GeneroController {
    private final GeneroService service;

    @GetMapping
    public List<CuentaPeliculasPorGenero> consultarGeneros() {
        return service.consultarGenerosConPeliculasEnCartelera();
    }
}
