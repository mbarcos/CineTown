package com.safa.cinetown.servicios;

import com.safa.cinetown.conversores.PeliculaMapper;
import com.safa.cinetown.dtos.PeliculaResponse;
import com.safa.cinetown.dtos.RecaudacionPelicula;
import com.safa.cinetown.dtos.request.PeliculaRequest;
import com.safa.cinetown.excepciones.RecursoNoEncontradoException;
import com.safa.cinetown.integraciones.tmdb.TmdbClient;
import com.safa.cinetown.modelos.Genero;
import com.safa.cinetown.modelos.Pelicula;
import com.safa.cinetown.repositorios.IGeneroRepository;
import com.safa.cinetown.repositorios.IPeliculaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class PeliculaService {
    private static final String PELICULA_NO_ENCONTRADA = "No se encontró la película";

    private final IPeliculaRepository repository;
    private final PeliculaMapper mapper;
    private final IGeneroRepository generoRepository;
    private final TmdbClient tmdbClient;
    private final TransactionTemplate transactionTemplate;

    @Transactional(readOnly = true)
    public List<PeliculaResponse> consultarTodasPeliculas() {
        return mapper.toResponseList(repository.findAllConGeneros());
    }

    @Transactional(readOnly = true)
    public List<PeliculaResponse> consultarTodasPeliculasActivas() {
        return mapper.toResponseList(repository.findByActivoTrueOrderByTituloAsc());
    }

    @Transactional(readOnly = true)
    public PeliculaResponse consultarPeliculaPorId(Integer id) {
        Pelicula pelicula = repository.findByIdConGeneros(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(PELICULA_NO_ENCONTRADA));
        return mapper.toResponse(pelicula);
    }

    @Transactional(readOnly = true)
    public RecaudacionPelicula consultarRecaudacionPorPelicula(Integer id) {
        return repository.calcularRecaudacionPorPelicula(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(PELICULA_NO_ENCONTRADA));
    }

    public Optional<String> buscarCartel(String titulo, int anio, String sinopsis) {
        return tmdbClient.buscarCartel(titulo, anio, sinopsis);
    }

    public PeliculaResponse crearPelicula(@Valid PeliculaRequest dto) {
        String cartelUrl = buscarCartel(dto.titulo(), dto.anio(), dto.sinopsis()).orElse(null);
        Pelicula pelicula = mapper.aEntidad(dto);
        pelicula.setActivo(dto.activo() == null || dto.activo());
        pelicula.setCartelUrl(cartelUrl);

        return transactionTemplate.execute(status -> {
            pelicula.setGeneros(new HashSet<>(buscarGeneros(dto.generos())));
            return mapper.toResponse(repository.save(pelicula));
        });
    }

    @Transactional
    public PeliculaResponse editarPelicula(Integer id, @Valid PeliculaRequest dto) {
        Pelicula pelicula = repository.findByIdConGeneros(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(PELICULA_NO_ENCONTRADA));
        List<Genero> generos = buscarGeneros(dto.generos());

        mapper.actualizarEntidad(dto, pelicula);
        pelicula.setGeneros(new HashSet<>(generos));

        Pelicula guardada = repository.save(pelicula);
        return mapper.toResponse(guardada);
    }

    public void cambiarEstadoCartelera(Integer id) {
        Pelicula pelicula = transactionTemplate.execute(status -> {
            Pelicula encontrada = repository.findByIdParaCambiarCartelera(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException(PELICULA_NO_ENCONTRADA));
            encontrada.setActivo(!Boolean.TRUE.equals(encontrada.getActivo()));
            return encontrada;
        });

        log.info("Estado de cartelera modificado: id={} activo={}", id, pelicula.getActivo());
        String cartelGuardado = pelicula.getCartelUrl();
        if (Boolean.TRUE.equals(pelicula.getActivo())
                && (cartelGuardado == null || cartelGuardado.trim().isEmpty())) {
            buscarCartel(pelicula.getTitulo(), pelicula.getAnio(), pelicula.getSinopsis())
                    .ifPresent(cartelUrl -> repository.guardarCartelSiFalta(
                            id, pelicula.getTitulo(), pelicula.getAnio(), pelicula.getSinopsis(), cartelUrl));
        }
    }

    private List<Genero> buscarGeneros(Set<String> nombresGeneros) {
        List<Genero> generos = generoRepository.findByNombreIn(nombresGeneros);
        if (generos.size() == nombresGeneros.size()) {
            return generos;
        }

        Set<String> nombresEncontrados = generos.stream()
                .map(Genero::getNombre)
                .collect(Collectors.toSet());
        Set<String> nombresNoEncontrados = new HashSet<>(nombresGeneros);
        nombresNoEncontrados.removeAll(nombresEncontrados);

        throw new RecursoNoEncontradoException(
                "No existen los géneros: " + String.join(", ", nombresNoEncontrados));
    }
}
