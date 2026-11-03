package com.safa.cinetown.controladores;

import com.safa.cinetown.dtos.PeliculaResponse;
import com.safa.cinetown.servicios.PeliculaService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/peliculas")
@AllArgsConstructor
public class PeliculaController {
    private PeliculaService service;
    @GetMapping
    public List<PeliculaResponse> consultarPeliculas(){
        return service.consultarTodasPeliculas();
    }

    @GetMapping("/{id}")
    public PeliculaResponse consultarPelicula(@PathVariable Integer id) {
        return service.consultarPelicula(id);
    }
}
