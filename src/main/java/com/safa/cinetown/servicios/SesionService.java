package com.safa.cinetown.servicios;

import com.safa.cinetown.dtos.SesionConAsientos;
import com.safa.cinetown.repositorios.ISesionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class SesionService {
    private final ISesionRepository repository;

    @Transactional(readOnly = true)
    public List<SesionConAsientos> consultarSesiones(LocalDate fecha, Integer peliculaId) {
        LocalDateTime inicio = null;
        LocalDateTime fin = null;

        if (fecha != null) {
            inicio = fecha.atStartOfDay();
            fin = fecha.plusDays(1).atStartOfDay();
        }

        return repository.encuentraSesionesActivasConAsientosLibres(inicio, fin, peliculaId);
    }
}
