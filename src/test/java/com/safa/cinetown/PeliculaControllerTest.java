package com.safa.cinetown;

import com.safa.cinetown.controladores.PeliculaController;
import com.safa.cinetown.dtos.PeliculaDetalleResponse;
import com.safa.cinetown.dtos.PeliculaResponse;
import com.safa.cinetown.excepciones.RecursoNoEncontradoException;
import com.safa.cinetown.modelos.ClasificacionEdad;
import com.safa.cinetown.servicios.PeliculaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class PeliculaControllerTest {
    private PeliculaService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(PeliculaService.class);
        mockMvc = standaloneSetup(new PeliculaController(service)).build();
    }

    @Test
    void consultaPeliculasActivasConSusGeneros() throws Exception {
        when(service.consultarTodasPeliculasActivas()).thenReturn(List.of(new PeliculaResponse(
                1, "Película de prueba", "Sinopsis", 120, 2025,
                ClasificacionEdad.P12, true, Set.of("Drama", "Comedia"))));

        mockMvc.perform(get("/peliculas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Película de prueba"))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[0].generos.length()").value(2))
                .andExpect(jsonPath("$[0].generos").isArray());

        verify(service).consultarTodasPeliculasActivas();
    }

    @Test
    void consultaUnaPeliculaPorId() throws Exception {
        when(service.consultarPeliculaPorId(1)).thenReturn(new PeliculaDetalleResponse(
                1, "Película de prueba", "Sinopsis", 120, 2025,
                ClasificacionEdad.P12, true, Set.of("Drama")));

        mockMvc.perform(get("/peliculas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Película de prueba"))
                .andExpect(jsonPath("$.generos[0]").value("Drama"))
                .andExpect(jsonPath("$.sesiones").doesNotExist());
    }

    @Test
    void peliculaInexistenteDevuelve404() throws Exception {
        when(service.consultarPeliculaPorId(404))
                .thenThrow(new RecursoNoEncontradoException("No se encontró la película con id 404"));

        mockMvc.perform(get("/peliculas/404"))
                .andExpect(status().isNotFound());
    }
}
