package com.safa.cinetown.controladores;

import com.safa.cinetown.dtos.SesionConAsientos;
import com.safa.cinetown.servicios.SesionService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/sesiones")
@AllArgsConstructor
public class SesionController {
    private final SesionService service;

    @GetMapping
    public List<SesionConAsientos> consultarSesiones(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Integer peliculaId) {
        return service.consultarSesiones(fecha, peliculaId);
    }

    @GetMapping("/{id}")
    public SesionConAsientos consultarSesionPorId(@PathVariable Integer id) {
        return service.consultarSesionPorId(id);
    }
}
