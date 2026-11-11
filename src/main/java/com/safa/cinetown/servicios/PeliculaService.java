package com.safa.cinetown.servicios;

import com.safa.cinetown.conversores.PeliculaMapper;
import com.safa.cinetown.dtos.PeliculaDetalleResponse;
import com.safa.cinetown.dtos.PeliculaResponse;
import com.safa.cinetown.dtos.RecaudacionPelicula;
import com.safa.cinetown.dtos.request.PeliculaRequest;
import com.safa.cinetown.excepciones.RecursoNoEncontradoException;
import com.safa.cinetown.modelos.Genero;
import com.safa.cinetown.modelos.Pelicula;
import com.safa.cinetown.repositorios.IGeneroRepository;
import com.safa.cinetown.repositorios.IPeliculaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.List;
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

    @Transactional(readOnly = true)
    public List<PeliculaResponse> consultarTodasPeliculas() {
        return mapper.toResponseList(repository.findAllConGeneros());
    }

    @Transactional(readOnly = true)
    public List<PeliculaResponse> consultarTodasPeliculasActivas() {
        return mapper.toResponseList(repository.findByActivoTrueOrderByTituloAsc());
    }

    @Transactional(readOnly = true)
    public PeliculaDetalleResponse consultarPeliculaPorId(Integer id) {
        Pelicula pelicula = repository.findByIdConGeneros(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(PELICULA_NO_ENCONTRADA));
        return mapper.toDetalleResponse(pelicula);
    }

    @Transactional(readOnly = true)
    public RecaudacionPelicula consultarRecaudacionPorPelicula(Integer id) {
        return repository.calcularRecaudacionPorPelicula(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(PELICULA_NO_ENCONTRADA));
    }

    @Transactional
    public PeliculaResponse crearPelicula(@Valid PeliculaRequest dto) {
        List<Genero> generos = buscarGeneros(dto.generos());
        Pelicula pelicula = mapper.aEntidad(dto);
        pelicula.setActivo(dto.activo() == null || dto.activo());
        pelicula.setGeneros(new HashSet<>(generos));

        Pelicula guardada = repository.save(pelicula);
        log.info("Película creada: id={} titulo={}", guardada.getId(), guardada.getTitulo());
        return mapper.toResponse(guardada);
    }

    @Transactional
    public PeliculaResponse editarPelicula(Integer id, @Valid PeliculaRequest dto) {
        Pelicula pelicula = repository.findByIdConGeneros(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(PELICULA_NO_ENCONTRADA));
        List<Genero> generos = buscarGeneros(dto.generos());

        pelicula.setTitulo(dto.titulo());
        pelicula.setSinopsis(dto.sinopsis());
        pelicula.setDuracion(dto.duracion());
        pelicula.setAnio(dto.anio());
        pelicula.setClasificacion(dto.clasificacion());
        if (dto.activo() != null) {
            pelicula.setActivo(dto.activo());
        }
        pelicula.setGeneros(new HashSet<>(generos));

        Pelicula guardada = repository.save(pelicula);
        log.info("Película modificada: id={} titulo={}", guardada.getId(), guardada.getTitulo());
        return mapper.toResponse(guardada);
    }

    @Transactional
    public void cambiarEstadoCartelera(Integer id) {
        Pelicula pelicula = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(PELICULA_NO_ENCONTRADA));

        pelicula.setActivo(!Boolean.TRUE.equals(pelicula.getActivo()));
        repository.save(pelicula);
        log.info("Estado de cartelera modificado: id={} activo={}", id, pelicula.getActivo());
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
