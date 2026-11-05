package com.safa.cinetown.servicios;

import com.safa.cinetown.conversores.PeliculaMapper;
import com.safa.cinetown.dtos.PeliculaDetalleResponse;
import com.safa.cinetown.dtos.PeliculaResponse;
import com.safa.cinetown.dtos.request.PeliculaRequest;
import com.safa.cinetown.excepciones.RecursoNoEncontradoException;
import com.safa.cinetown.modelos.Pelicula;
import com.safa.cinetown.modelos.Sesion;
import com.safa.cinetown.repositorios.IPeliculaRepository;
import com.safa.cinetown.repositorios.ISesionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class PeliculaService {
    private IPeliculaRepository repository;
    private ISesionRepository sesionRepository;
    private PeliculaMapper mapper;

    @Transactional(readOnly = true)
    public List<PeliculaResponse> consultarTodasPeliculas() {
        return mapper.toResponseList(repository.findAllConGeneros());
    }

    @Transactional(readOnly = true)
    public PeliculaDetalleResponse consultarPelicula(Integer id) {
        Pelicula pelicula = repository.findByIdConGeneros(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la película"));
        List<Sesion> sesiones = sesionRepository.findByPeliculaIdOrderByFechaHoraInicioAsc(id);
        return mapper.toDetalleResponse(pelicula, sesiones);
    }
    public void crearPelicula(PeliculaRequest dto) {

    }
}
