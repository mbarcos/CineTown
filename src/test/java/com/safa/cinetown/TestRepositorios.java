package com.safa.cinetown;

import com.safa.cinetown.modelos.Pelicula;
import com.safa.cinetown.repositorios.IPeliculaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TestRepositorios {

    @Autowired
    private IPeliculaRepository peliculaRepository;

    @Test
    void encuentraLasPeliculasActivas() {
        List<Pelicula> activos = peliculaRepository.findAll();


    }
}
