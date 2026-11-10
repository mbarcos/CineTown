package com.safa.cinetown.servicios;

import com.safa.cinetown.dtos.CuentaPeliculasPorGenero;
import com.safa.cinetown.repositorios.IGeneroRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class GeneroService {
    private final IGeneroRepository repository;

    @Transactional(readOnly = true)
    public List<CuentaPeliculasPorGenero> consultarGenerosConPeliculasEnCartelera() {
        return repository.contarPeliculasEnCarteleraPorGenero();
    }
}
