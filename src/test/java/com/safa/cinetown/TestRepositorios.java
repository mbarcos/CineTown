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

import java.time.LocalDate;
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

    // encuentra todas las peliculas por titulo (este no lo utilizo nunca)
    @Test
    void encuentraLasPeliculasPorTitulo() {
        List<Pelicula> resultado = peliculaRepository.findByTituloContainingIgnoreCase("interstellar");
        assertThat(resultado).isNotEmpty();
        resultado.forEach(p -> System.out.println(p.getTitulo()));
    }

    @Test
    void paginaElCatalogoSinDuplicadosYCombinaFiltros() {
        PageRequest primera = PageRequest.of(0, 1);
        Page<Pelicula> pagina = peliculaRepository.buscarCatalogo("", "", null, null, null, primera);
        Page<Pelicula> siguiente = peliculaRepository.buscarCatalogo("", "", null, null, null, primera.next());
        assertThat(pagina.getTotalElements()).isGreaterThan(1);
        Pelicula pelicula = pagina.getContent().getFirst();
        assertThat(siguiente.getContent().getFirst().getId()).isNotEqualTo(pelicula.getId());

        String genero = pelicula.getGeneros().iterator().next().getNombre();
        Page<Pelicula> filtradas = peliculaRepository.buscarCatalogo(genero, genero, pelicula.getActivo(),
                pelicula.getClasificacion(), pelicula.getDuracion(), primera);
        assertThat(filtradas.getContent()).extracting(Pelicula::getId).containsExactly(pelicula.getId());
    }

    // consulta para /pelicula/id, muestra tambien los generos de la pelicula
    @Test
    void encuentraUnaPeliculaConSusGeneros() {
        Optional<Pelicula> resultado = peliculaRepository.findByIdConGeneros(1);

        assertThat(resultado).isPresent();
        Pelicula pelicula = resultado.orElseThrow();
        assertThat(pelicula.getId()).isEqualTo(1);
        assertThat(pelicula.getGeneros()).isNotNull();
        System.out.println(pelicula.getTitulo() + " | Géneros: "
                + pelicula.getGeneros().stream()
                .map(g -> g.getNombre())
                .toList());
    }

    // consulta utilizada en GET /peliculas (solo encuentra las activas)
    @Test
    void encuentraLasPeliculasActivasOrdenadas() {
        List<Pelicula> resultado = peliculaRepository.findByActivoTrueOrderByTituloAsc();
        assertThat(resultado).isNotEmpty();
        assertThat(resultado).allMatch(Pelicula::getActivo);
        resultado.forEach(p -> System.out.println(p.getTitulo() + " - " + p.getSinopsis()));
    }

    // consulta para GET /generos
    @Test
    void cuentaPeliculasEnCarteleraPorGenero() {
        List<CuentaPeliculasPorGenero> resultado = generoRepository.contarPeliculasEnCarteleraPorGenero();
        assertThat(resultado).isNotEmpty();
        resultado.forEach(g ->
                System.out.println(g.nombre() + ": " + g.peliculasEnCartelera()));
    }

    // para /salas
    @Test
    void encontrarSalasConCapacidades() {
        List<Sala> salas = salaRepository.findAllByOrderByNumeroAsc();

        assertThat(salas).isNotEmpty();
        salas.forEach(s ->
                System.out.println("Sala " + s.getNumero() + ": " + s.getCapacidad() + " asientos"));
    }

    // para /sesiones?fecha=&peliculaId= (no pongo id asi que salen todas y fecha a partir de hoy)
    @Test
    void encuentraSesionesActivasConAsientosLibres() {
        List<SesionConAsientos> resultado = sesionRepository.encuentraSesionesActivasConAsientosLibres();
        assertThat(resultado).isNotEmpty();
        resultado.forEach(s ->
                System.out.println(s.titulo() + " | Sala " + s.numeroSala()
                        + " | " + s.fechaHoraInicio()
                        + " | Libres: " + s.asientosLibres() + "/" + s.capacidad()));
    }

    // para /sesiones/{id}
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

    //consulta para GET /compras/{codigo}
    @Test
    void encuentraLosDetallesDeUnaCompra() {
        Optional<DetalleCompra> detalle = compraRepository.encuentraDetalleCompraPorCodigo("W8HM3AFO");
        assertThat(detalle).isPresent();
        detalle.ifPresent(d -> System.out.println(
                "Código: " + d.codCompra()
                        + " | Película: " + d.titulo()
                        + " | Sala " + d.numeroSala()
                        + " | Inicio: " + d.fechaHoraInicio()));
    }

    //para /compras, deben salir todas las compras. La de abajo es la que uso para un usuario especifico
    @Test
    void encuentraTodasLasComprasPaginadas() {
        Page<CompraResumen> pagina = compraRepository.encuentraTodasLasCompras(PageRequest.of(0, 3));
        assertThat(pagina.getContent()).isNotEmpty();
        pagina.forEach(c -> System.out.println(
                c.codCompra() + " | " + c.titulo() + " | Sala " + c.numeroSala()));
    }

    // /compras de un usuario especifico
    @Test
    void encuentraLasComprasDeUnUsuario() {
        Page<CompraResumen> pagina = compraRepository.encuentraComprasDeUsuario(4, PageRequest.of(0, 5));
        assertThat(pagina.getContent()).isNotEmpty();
        pagina.forEach(c -> System.out.println(
                c.codCompra() + " | " + c.titulo() + " | Sala " + c.numeroSala()));
    }

    // para /peliculas/{id}/recaudacion
    @Test
    void calculaLaRecaudacionDeUnaPelicula() {
        Optional<RecaudacionPelicula> recaudacion = peliculaRepository.calcularRecaudacionPorPelicula(1);

        assertThat(recaudacion).isPresent();
        recaudacion.ifPresent(r -> System.out.println(
                r.titulo() + " | Entradas: " + r.entradasVendidas() + " | Total: " + r.recaudacionTotal()));
    }

    // /informes/ocupacion?fecha=
    @Test
    void calculaLaOcupacionDeLasSesionesDeUnDia() {
        LocalDate fecha = LocalDate.of(2026, 11, 14);;

        List<OcupacionPorSesion> ocupacion = sesionRepository.calcularOcupacionPorSesion(
                fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());

        assertThat(ocupacion).isNotEmpty();
        assertThat(ocupacion).allMatch(o -> o.fechaHoraInicio().toLocalDate().equals(fecha));
        assertThat(ocupacion).allMatch(o -> o.porcentajeOcupacion() >= 0 && o.porcentajeOcupacion() <= 100);
        ocupacion.forEach(o -> System.out.println(
                o.titulo() + " | Sala " + o.numeroSala()
                        + " | " + o.entradasVendidas() + "/" + o.capacidad()
                        + " | " + o.porcentajeOcupacion() + "%"));
    }



}
