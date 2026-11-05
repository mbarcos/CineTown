package com.safa.cinetown.servicios;

import com.safa.cinetown.conversores.PeliculaMapper;
import com.safa.cinetown.dtos.PeliculaDetalleResponse;
import com.safa.cinetown.dtos.PeliculaResponse;
import com.safa.cinetown.dtos.request.PeliculaRequest;
import com.safa.cinetown.excepciones.RecursoNoEncontradoException;
import com.safa.cinetown.modelos.Genero;
import com.safa.cinetown.modelos.Pelicula;
import com.safa.cinetown.modelos.Sesion;
import com.safa.cinetown.repositorios.IGeneroRepository;
import com.safa.cinetown.repositorios.IPeliculaRepository;
import com.safa.cinetown.repositorios.ISesionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class PeliculaService {
    private IPeliculaRepository repository;
    private ISesionRepository sesionRepository;
    private PeliculaMapper mapper;
    private IGeneroRepository generoRepository;

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
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la película"));
        List<Sesion> sesiones = sesionRepository.findByPeliculaIdOrderByFechaHoraInicioAsc(id);
        return mapper.toDetalleResponse(pelicula, sesiones);
    }

    @Transactional
    public PeliculaResponse crearPelicula(PeliculaRequest dto) {
        //Comprobar que existen los generos
        List<Genero> generos = generoRepository.findByNombreIn(dto.generos());
        if (generos.size() != dto.generos().size()) {
            Set<String> encontrados = generos.stream()
                    .map(Genero::getNombre)
                    .collect(Collectors.toSet());
            Set<String> noEncontrados = new HashSet<>(dto.generos());
            noEncontrados.removeAll(encontrados);
            throw new RecursoNoEncontradoException(
                    "No existen los géneros: " + String.join(", ", noEncontrados));
        }

        Pelicula pelicula = mapper.aEntidad(dto);
        pelicula.setActivo(dto.activo() == null || dto.activo());
        pelicula.setGeneros(new HashSet<>(generos));

        Pelicula guardada = repository.save(pelicula);
        log.info("Película creada: id={} titulo={}", guardada.getId(), guardada.getTitulo());
        return mapper.toResponse(guardada);


    }
}
