package com.safa.cinetown.controladores;

import com.safa.cinetown.dtos.PeliculaResponse;
import com.safa.cinetown.dtos.PeliculaDetalleResponse;
import com.safa.cinetown.dtos.request.PeliculaRequest;
import com.safa.cinetown.servicios.PeliculaService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public PeliculaDetalleResponse consultarPelicula(@PathVariable Integer id) {
        return service.consultarPelicula(id);
    }

    @PostMapping("/crear")
    public void crearEspecie(@Valid @RequestBody PeliculaRequest json){
        service.crearEspeciePez(json);
    }
}
