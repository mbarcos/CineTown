package com.safa.cinetown.controladores;

import com.safa.cinetown.dtos.SesionConAsientos;
import com.safa.cinetown.dtos.OcupacionPorSesion;
import com.safa.cinetown.dtos.request.SesionRequest;
import com.safa.cinetown.servicios.SesionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
public class SesionController {
    private final SesionService service;

    @PostMapping("/sesiones")
    public void programarSesion(@Valid @RequestBody SesionRequest request) {
        service.programarSesion(request);
    }

    @GetMapping("/sesiones")
    public List<SesionConAsientos> consultarSesiones(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Integer peliculaId) {
        return service.consultarSesiones(fecha, peliculaId);
    }

    @GetMapping("/sesiones/{id}")
    public SesionConAsientos consultarSesionPorId(@PathVariable Integer id) {
        return service.consultarSesionPorId(id);
    }

    @GetMapping("/informes/ocupacion")
    public List<OcupacionPorSesion> consultarOcupacion(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.consultarOcupacion(fecha);
    }
}
