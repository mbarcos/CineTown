package com.safa.cinetown;

import com.safa.cinetown.dtos.request.PeliculaRequest;
import com.safa.cinetown.integraciones.tmdb.TmdbClient;
import com.safa.cinetown.modelos.ClasificacionEdad;
import com.safa.cinetown.servicios.PeliculaService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.mail.host=localhost",
        "spring.mail.username=test",
        "spring.mail.password=test",
        "app.correo.remitente=test@example.com"
})
class CineTownApplicationTests {

    @Autowired
    private PeliculaService peliculaService;

    @Autowired
    private EntityManager entityManager;

    @MockitoSpyBean
    private TmdbClient tmdbClient;

    @Test
    void contextLoads() {
    }

    @Test
    @Transactional
    void elAltaPersisteElCartelEncontradoEnTmdb() {
        String titulo = "Película de prueba del cartel";
        String cartelUrl = "https://image.tmdb.org/t/p/w500/cartel.jpg";
        doReturn(Optional.of(cartelUrl)).when(tmdbClient).buscarCartel(titulo, 2026, null);
        PeliculaRequest alta = new PeliculaRequest(titulo, null, 100, 2026,
                ClasificacionEdad.TP, true, Set.of("Drama"));

        Integer peliculaId = peliculaService.crearPelicula(alta).id();
        entityManager.flush();
        entityManager.clear();

        assertThat(peliculaService.consultarPeliculaPorId(peliculaId).cartelUrl()).isEqualTo(cartelUrl);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    @Transactional
    void ponerEnCarteleraCompletaUnCartelFaltanteYConservaElGuardado(String cartelPrevio) {
        String titulo = "Película de prueba de cartelera";
        String cartelUrl = "https://image.tmdb.org/t/p/w500/cartel.jpg";
        String sinopsis = "Sinopsis de la película de prueba.";
        doReturn(Optional.empty(), Optional.of(cartelUrl)).when(tmdbClient).buscarCartel(titulo, 2026, sinopsis);
        PeliculaRequest alta = new PeliculaRequest(titulo, sinopsis, 100, 2026,
                ClasificacionEdad.TP, false, Set.of("Drama"));
        Integer peliculaId = peliculaService.crearPelicula(alta).id();
        entityManager.flush();
        entityManager.createQuery("UPDATE Pelicula p SET p.cartelUrl = :cartelUrl WHERE p.id = :id")
                .setParameter("cartelUrl", cartelPrevio)
                .setParameter("id", peliculaId)
                .executeUpdate();
        entityManager.clear();

        peliculaService.cambiarEstadoCartelera(peliculaId);
        entityManager.clear();
        var activada = peliculaService.consultarPeliculaPorId(peliculaId);
        assertThat(activada.activo()).isTrue();
        assertThat(activada.cartelUrl()).isEqualTo(cartelUrl);

        peliculaService.cambiarEstadoCartelera(peliculaId);
        peliculaService.cambiarEstadoCartelera(peliculaId);
        entityManager.flush();
        entityManager.clear();
        assertThat(peliculaService.consultarPeliculaPorId(peliculaId).cartelUrl()).isEqualTo(cartelUrl);
        verify(tmdbClient, times(2)).buscarCartel(titulo, 2026, sinopsis);
    }

}
