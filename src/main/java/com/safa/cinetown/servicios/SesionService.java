package com.safa.cinetown.servicios;

import com.safa.cinetown.conversores.SesionMapper;
import com.safa.cinetown.dtos.OcupacionPorSesion;
import com.safa.cinetown.dtos.SesionConAsientos;
import com.safa.cinetown.dtos.SesionGestionResponse;
import com.safa.cinetown.dtos.request.SesionRequest;
import com.safa.cinetown.excepciones.ConflictoException;
import com.safa.cinetown.excepciones.ReglaNegocioException;
import com.safa.cinetown.excepciones.RecursoNoEncontradoException;
import com.safa.cinetown.modelos.Pelicula;
import com.safa.cinetown.modelos.Sala;
import com.safa.cinetown.modelos.Sesion;
import com.safa.cinetown.repositorios.ICompraRepository;
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
    private static final long MINUTOS_LIMPIEZA = 20;
    private static final int ID_PELICULA_SIN_FILTRO = -1;

    private final ISesionRepository repository;
    private final IPeliculaRepository peliculaRepository;
    private final ISalaRepository salaRepository;
    private final ICompraRepository compraRepository;
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

    @Transactional
    public void modificarSesion(Integer id, SesionRequest request) {
        Sesion sesion = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la sesión"));
        validarFechaFutura(request.fechaHoraInicio());
        Pelicula pelicula = obtenerPeliculaParaModificar(request.peliculaId(), sesion.getPelicula().getId());
        Sala sala = obtenerSala(request.salaId());
        long entradasVendidas = compraRepository.cuentaEntradasActivasPorSesion(sesion.getId());
        if (entradasVendidas > sala.getCapacidad()) {
            throw new ReglaNegocioException(
                    "La sala seleccionada no tiene capacidad suficiente para las entradas vendidas");
        }
        LocalDateTime fin = calcularFin(request.fechaHoraInicio(), pelicula);
        validarDisponibilidadSala(sesion.getId(), sala, request.fechaHoraInicio(), fin);

        mapper.actualizarEntidad(request, pelicula, sala, fin, sesion);
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
                    "La película no está en cartelera");
        }
        return pelicula;
    }

    private Pelicula obtenerPeliculaParaModificar(Integer peliculaId, Integer peliculaActualId) {
        if (peliculaId.equals(peliculaActualId)) {
            return peliculaRepository.findById(peliculaId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la película"));
        }
        return obtenerPeliculaActiva(peliculaId);
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
        LocalDateTime inicioConMargen = inicio.minusMinutes(MINUTOS_LIMPIEZA);
        LocalDateTime finConMargen = fin.plusMinutes(MINUTOS_LIMPIEZA);
        if (repository.existeSolapamientoEnSala(
                sala.getId(), inicioConMargen, finConMargen)) {
            throw new ConflictoException(
                    "La sala ya tiene una sesión solapada, incluyendo los 20 minutos de limpieza");
        }
    }

    private void validarDisponibilidadSala(
            Integer sesionId, Sala sala, LocalDateTime inicio, LocalDateTime fin) {
        LocalDateTime inicioConMargen = inicio.minusMinutes(MINUTOS_LIMPIEZA);
        LocalDateTime finConMargen = fin.plusMinutes(MINUTOS_LIMPIEZA);
        if (repository.existeSolapamientoEnSalaExcluyendoSesion(
                sesionId, sala.getId(), inicioConMargen, finConMargen)) {
            throw new ConflictoException(
                    "La sala ya tiene una sesión solapada, incluyendo los 20 minutos de limpieza");
        }
    }

    @Transactional(readOnly = true)
    public List<SesionConAsientos> consultarSesiones(LocalDate fecha, Integer peliculaId) {
        LocalDateTime inicioDelRango = calcularInicioDelRango(fecha);
        LocalDateTime finDelRango = calcularFinDelRango(fecha, inicioDelRango);

        return repository.encuentraSesionesActivasConAsientosLibres(
                inicioDelRango,
                finDelRango,
                obtenerIdPeliculaFiltro(peliculaId),
                fecha != null,
                peliculaId != null);
    }

    private LocalDateTime calcularInicioDelRango(LocalDate fecha) {
        if (fecha == null) {
            return LocalDateTime.now();
        }
        return fecha.atStartOfDay();
    }

    private LocalDateTime calcularFinDelRango(
            LocalDate fecha, LocalDateTime inicioDelRango) {
        if (fecha == null) {
            return inicioDelRango;
        }
        return fecha.plusDays(1).atStartOfDay();
    }

    private Integer obtenerIdPeliculaFiltro(Integer peliculaId) {
        if (peliculaId == null) {
            return ID_PELICULA_SIN_FILTRO;
        }
        return peliculaId;
    }

    @Transactional(readOnly = true)
    public SesionConAsientos consultarSesionPorId(Integer id) {
        return repository.encuentraDetallePorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la sesión"));
    }

    @Transactional(readOnly = true)
    public List<SesionGestionResponse> consultarSesionesParaGestion() {
        return repository.encuentraSesionesParaGestion();
    }

    @Transactional(readOnly = true)
    public List<OcupacionPorSesion> consultarOcupacion(LocalDate fecha) {
        return repository.calcularOcupacionPorSesion(
                fecha.atStartOfDay(),
                fecha.plusDays(1).atStartOfDay());
    }
}
