package com.safa.cinetown.controladores;

import com.safa.cinetown.dtos.PeliculaResponse;
import com.safa.cinetown.dtos.RecaudacionPelicula;
import com.safa.cinetown.dtos.request.PeliculaRequest;
import com.safa.cinetown.servicios.PeliculaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
        return service.consultarTodasPeliculasActivas();
    }

    @GetMapping("/gestion")
    public List<PeliculaResponse> consultarPeliculasParaGestion() {
        return service.consultarTodasPeliculas();
    }

    @PostMapping("/gestion/cartel")
    public CartelResponse previsualizarCartel(@Valid @RequestBody CartelRequest datos) {
        return new CartelResponse(service.buscarCartel(datos.titulo(), datos.anio(), datos.sinopsis()).orElse(null));
    }

    public record CartelRequest(@NotBlank @Size(max = 255) String titulo,
                                @NotNull @Min(1888) @Max(2100) Integer anio,
                                String sinopsis) {
    }

    public record CartelResponse(String cartelUrl) {
    }

    @GetMapping("/{id}")
    public PeliculaResponse consultarPeliculaId(@PathVariable Integer id) {
        return service.consultarPeliculaPorId(id);
    }

    @GetMapping("/{id}/recaudacion")
    public RecaudacionPelicula consultarRecaudacion(@PathVariable Integer id) {
        return service.consultarRecaudacionPorPelicula(id);
    }

    @PostMapping
    public void crearPelicula(@Valid @RequestBody PeliculaRequest json){
        service.crearPelicula(json);
    }

    @PutMapping("/{id}")
    public PeliculaResponse editarPelicula(@PathVariable Integer id,
                                           @Valid @RequestBody PeliculaRequest json) {
        return service.editarPelicula(id, json);
    }

    @PatchMapping("/{id}/cartelera")
    public void cambiarEstadoCartelera(@PathVariable Integer id) {
        service.cambiarEstadoCartelera(id);
    }
}
