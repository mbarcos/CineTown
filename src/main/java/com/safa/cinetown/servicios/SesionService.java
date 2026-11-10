package com.safa.cinetown.servicios;

import com.safa.cinetown.dtos.SesionConAsientos;
import com.safa.cinetown.dtos.OcupacionPorSesion;
import com.safa.cinetown.excepciones.RecursoNoEncontradoException;
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
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicio = ahora;
        LocalDateTime fin = ahora;

        if (fecha != null) {
            inicio = fecha.atStartOfDay();
            fin = fecha.plusDays(1).atStartOfDay();
        }

        int idPelicula = peliculaId == null ? -1 : peliculaId;

        return repository.encuentraSesionesActivasConAsientosLibres(
                inicio,
                fin,
                idPelicula,
                fecha != null,
                peliculaId != null);
    }

    @Transactional(readOnly = true)
    public SesionConAsientos consultarSesionPorId(Integer id) {
        return repository.encuentraDetallePorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la sesión"));
    }

    @Transactional(readOnly = true)
    public List<OcupacionPorSesion> consultarOcupacion(LocalDate fecha) {
        return repository.calcularOcupacionPorSesion(
                fecha.atStartOfDay(),
                fecha.plusDays(1).atStartOfDay());
    }
}
