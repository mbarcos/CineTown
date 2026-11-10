package com.safa.cinetown.servicios;

import com.safa.cinetown.conversores.SesionMapper;
import com.safa.cinetown.dtos.OcupacionPorSesion;
import com.safa.cinetown.dtos.SesionConAsientos;
import com.safa.cinetown.dtos.request.SesionRequest;
import com.safa.cinetown.excepciones.ConflictoException;
import com.safa.cinetown.excepciones.ReglaNegocioException;
import com.safa.cinetown.excepciones.RecursoNoEncontradoException;
import com.safa.cinetown.modelos.Pelicula;
import com.safa.cinetown.modelos.Sala;
import com.safa.cinetown.modelos.Sesion;
import com.safa.cinetown.repositorios.IPeliculaRepository;
import com.safa.cinetown.repositorios.ISalaRepository;
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
    private final IPeliculaRepository peliculaRepository;
    private final ISalaRepository salaRepository;
    private final SesionMapper mapper;

    @Transactional
    public void programarSesion(SesionRequest request) {
        validarFechaFutura(request.fechaHoraInicio());
        Pelicula pelicula = obtenerPeliculaActiva(request.peliculaId());
        Sala sala = obtenerSala(request.salaId());
        LocalDateTime fin = calcularFin(request.fechaHoraInicio(), pelicula);
        validarDisponibilidadSala(sala, request.fechaHoraInicio(), fin);

        Sesion sesion = mapper.aEntidad(request, pelicula, sala, fin);
        repository.save(sesion);
    }

    private void validarFechaFutura(LocalDateTime inicio) {
        if (!inicio.isAfter(LocalDateTime.now())) {
            throw new ReglaNegocioException("La sesión debe tener una fecha futura");
        }
    }

    private Pelicula obtenerPeliculaActiva(Integer peliculaId) {
        Pelicula pelicula = peliculaRepository.findById(peliculaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la película"));
        if (!Boolean.TRUE.equals(pelicula.getActivo())) {
            throw new ReglaNegocioException(
                    "Solo se pueden programar sesiones de películas activas");
        }
        return pelicula;
    }

    private Sala obtenerSala(Integer salaId) {
        return salaRepository.findById(salaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la sala"));
    }

    private LocalDateTime calcularFin(LocalDateTime inicio, Pelicula pelicula) {
        return inicio.plusMinutes(pelicula.getDuracion());
    }

    private void validarDisponibilidadSala(
            Sala sala, LocalDateTime inicio, LocalDateTime fin) {
        LocalDateTime inicioConMargen = inicio.minusMinutes(20);
        LocalDateTime finConMargen = fin.plusMinutes(20);
        if (repository.existeSolapamientoEnSala(
                sala.getId(), inicioConMargen, finConMargen)) {
            throw new ConflictoException(
                    "La sala ya tiene una sesión solapada, incluyendo los 20 minutos de limpieza");
        }
    }

    @Transactional(readOnly = true)
    public List<SesionConAsientos> consultarSesiones(LocalDate fecha, Integer peliculaId) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicio = fecha == null ? ahora : fecha.atStartOfDay();
        LocalDateTime fin = fecha == null
                ? ahora
                : fecha.plusDays(1).atStartOfDay();

        return repository.encuentraSesionesActivasConAsientosLibres(
                inicio,
                fin,
                peliculaId == null ? -1 : peliculaId,
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
