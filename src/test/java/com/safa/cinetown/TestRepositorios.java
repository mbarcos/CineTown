package com.safa.cinetown;

import com.safa.cinetown.dtos.*;
import com.safa.cinetown.modelos.Pelicula;
import com.safa.cinetown.modelos.Sala;
import com.safa.cinetown.modelos.Sesion;
import com.safa.cinetown.repositorios.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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
        Optional<DetalleCompra> detalle = compraRepository.encuentraDetalleCompraPorCodigo("ABC12345");
        assertThat(detalle).isPresent();
        detalle.ifPresent(d -> System.out.println(
                "Código: " + d.codCompra()
                        + " | Película: " + d.titulo()
                        + " | Sala " + d.numeroSala()
                        + " | Inicio: " + d.fechaHoraInicio()));
    }

    @Test
    void encuentraTodasLasComprasPaginadas() {
        Page<CompraResumen> pagina = compraRepository.encuentraTodasLasCompras(PageRequest.of(0, 3));
        assertThat(pagina.getContent()).isNotEmpty();
        pagina.forEach(c -> System.out.println(
                c.codCompra() + " | " + c.titulo() + " | Sala " + c.numeroSala()));
    }

    @Test
    void encuentraLasComprasDeUnUsuario() {
        Page<CompraResumen> pagina = compraRepository.encuentraComprasDeUsuario(4, PageRequest.of(0, 5));
        assertThat(pagina.getContent()).isNotEmpty();
        pagina.forEach(c -> System.out.println(
                c.codCompra() + " | " + c.titulo() + " | Sala " + c.numeroSala()));
    }

    @Test
    void calculaLaRecaudacionDeUnaPelicula() {
        Optional<RecaudacionPelicula> recaudacion = peliculaRepository.calcularRecaudacionPorPelicula(1);

        assertThat(recaudacion).isPresent();
        recaudacion.ifPresent(r -> System.out.println(
                r.titulo() + " | Entradas: " + r.entradasVendidas() + " | Total: " + r.recaudacionTotal()));
    }
}
