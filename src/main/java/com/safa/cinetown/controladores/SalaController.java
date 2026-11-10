package com.safa.cinetown.controladores;

import com.safa.cinetown.modelos.Sala;
import com.safa.cinetown.servicios.SalaService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/salas")
@AllArgsConstructor
public class SalaController {
    private final SalaService service;

    @GetMapping
    public List<Sala> consultarSalas() {
        return service.consultarSalas();
    }
}
