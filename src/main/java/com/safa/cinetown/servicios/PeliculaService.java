package com.safa.cinetown.servicios;

import com.safa.cinetown.conversores.PeliculaMapper;
import com.safa.cinetown.dtos.PeliculaResponse;
import com.safa.cinetown.excepciones.RecursoNoEncontradoException;
import com.safa.cinetown.repositorios.IPeliculaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class PeliculaService {
    private IPeliculaRepository repository;
    private PeliculaMapper mapper;

    @Transactional(readOnly = true)
    public List<PeliculaResponse> consultarTodasPeliculas() {
        return mapper.toResponseList(repository.findAllConGeneros());
    }

    @Transactional(readOnly = true)
    public PeliculaResponse consultarPelicula(Integer id) {
        return repository.findByIdConGeneros(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la película con id " + id));
    }
}
