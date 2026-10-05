package com.safa.cinetown;

import com.safa.cinetown.dtos.CuentaPeliculasPorGenero;
import com.safa.cinetown.dtos.SesionConAsientos;
import com.safa.cinetown.modelos.Compra;
import com.safa.cinetown.modelos.Pelicula;
import com.safa.cinetown.modelos.Sala;
import com.safa.cinetown.modelos.Sesion;
import com.safa.cinetown.repositorios.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;
import java.util.Optional;

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

    @Autowired
    private ICompraRepository compraRepository;

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

    @Test
    void encuentraLasSesionesDeUnaPelicula() {
        List<Sesion> sesiones = sesionRepository.findByPeliculaIdOrderByFechaHoraInicioAsc(1);

        assertThat(sesiones).isNotEmpty();
        sesiones.forEach(s -> System.out.println(
                s.getPelicula().getTitulo()
                        + " | Inicio: " + s.getFechaHoraInicio()
                        + " | Fin: " + s.getFechaHoraFin()
                        + " | Precio: " + s.getPrecioBase()));
    }

    @Test
    void encuentraLosDetallesDeUnaCompra() {
        Optional<Compra> compra = compraRepository.findByCodCompra("ABC12345");

        assertThat(compra).isPresent();
        compra.ifPresent(c -> System.out.println(
                "Código: " + c.getCodCompra()
                        + " | Película: " + c.getSesion().getPelicula().getTitulo()
                        + " | Sala " + c.getSesion().getSala().getNumero()
                        + " | Inicio: " + c.getSesion().getFechaHoraInicio()));
    }
}
