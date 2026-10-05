package com.safa.cinetown;

import com.safa.cinetown.dtos.CuentaPeliculasPorGenero;
import com.safa.cinetown.dtos.SesionConAsientos;
import com.safa.cinetown.modelos.Pelicula;
import com.safa.cinetown.modelos.Sala;
import com.safa.cinetown.repositorios.IGeneroRepository;
import com.safa.cinetown.repositorios.IPeliculaRepository;
import com.safa.cinetown.repositorios.ISalaRepository;
import com.safa.cinetown.repositorios.ISesionRepository;
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

    @Autowired
    private IGeneroRepository generoRepository;

    @Autowired
    private ISalaRepository salaRepository;

    @Autowired
    private ISesionRepository sesionRepository;

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
        resultado.forEach(p -> System.out.println(p.getTitulo() + " - " + p.getSinopsis()));
    }

    @Test
    void cuentaPeliculasEnCarteleraPorGenero() {
        List<CuentaPeliculasPorGenero> resultado = generoRepository.contarPeliculasEnCarteleraPorGenero();
        assertThat(resultado).isNotEmpty();
        resultado.forEach(g ->
                System.out.println(g.nombre() + ": " + g.peliculasEnCartelera()));
    }

    @Test
    void encontrarSalasConCapacidades() {
        List<Sala> salas = salaRepository.findAllByOrderByNumeroAsc();

        assertThat(salas).isNotEmpty();
        salas.forEach(s ->
                System.out.println("Sala " + s.getNumero() + ": " + s.getCapacidad() + " asientos"));
    }

    @Test
    void encuentraSesionesActivasConAsientosLibres() {
        List<SesionConAsientos> resultado = sesionRepository.encuentraSesionesActivasConAsientosLibres();

        assertThat(resultado).isNotEmpty();
        resultado.forEach(s ->
                System.out.println(s.titulo() + " | Sala " + s.numeroSala()
                        + " | " + s.fechaHoraInicio()
                        + " | Libres: " + s.asientosLibres() + "/" + s.capacidad()));
    }

}
