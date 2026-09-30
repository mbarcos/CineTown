package com.safa.cinetown;

import com.safa.cinetown.modelos.Pelicula;
import com.safa.cinetown.repositorios.IPeliculaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TestRepositorios {

    @Autowired
    private IPeliculaRepository peliculaRepository;

    @Test
    void encuentraLasPeliculasPorTitulo() {
        List<Pelicula> resultado = peliculaRepository.findByTituloContainingIgnoreCase("interstellar");
        assertThat(resultado).isNotEmpty();
        resultado.forEach(p -> System.out.println(p.getTitulo()));
    }

    @Test
    void encuentraLasPeliculasActivasOrdenadas() {
        List<Pelicula> resultado = peliculaRepository.findByActivoTrueOrderByTituloAsc();

        assertThat(resultado).isNotEmpty();
        assertThat(resultado).allMatch(Pelicula::getActivo);
        resultado.forEach(p -> System.out.println(p.getTitulo()));
    }
}
