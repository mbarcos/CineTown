package com.safa.cinetown.servicios;

import com.safa.cinetown.modelos.Sala;
import com.safa.cinetown.repositorios.ISalaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class SalaService {
    private final ISalaRepository repository;

    @Transactional(readOnly = true)
    public List<Sala> consultarSalas() {
        return repository.findAllByOrderByNumeroAsc();
    }
}
