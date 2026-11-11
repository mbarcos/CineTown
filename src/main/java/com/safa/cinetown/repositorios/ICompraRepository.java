package com.safa.cinetown.repositorios;

import com.safa.cinetown.dtos.CompraResumen;
import com.safa.cinetown.dtos.DetalleCompra;
import com.safa.cinetown.modelos.Compra;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface ICompraRepository extends JpaRepository <Compra, Integer>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Compra c WHERE c.codCompra = :codCompra")
    Optional<Compra> encuentraPorCodigoParaActualizar(@Param("codCompra") String codCompra);

    @Query("SELECT COALESCE(SUM(c.numEntradas), 0) FROM Compra c WHERE c.sesion.id = :sesionId AND c.estado = true")
    long cuentaEntradasActivasPorSesion(@Param("sesionId") Integer sesionId);

    boolean existsByCodCompra(String codCompra);

    @Query("""
            SELECT new com.safa.cinetown.dtos.DetalleCompra(
                   c.codCompra, sa.numero, s.fechaHoraInicio, p.titulo)
            FROM Compra c
            JOIN c.sesion s
            JOIN s.pelicula p
            JOIN s.sala sa
            WHERE c.codCompra = :codCompra
            """)
    Optional<DetalleCompra> encuentraDetalleCompraPorCodigo(@Param("codCompra") String codCompra);

    boolean existsByCodCompraAndUsuario_Id(String codCompra, Integer usuarioId);

    @Query(value = """
        SELECT new com.safa.cinetown.dtos.CompraResumen(
               c.codCompra, p.titulo, sa.numero, s.fechaHoraInicio,
               c.numEntradas, c.precioTotal, c.estado, c.fechaCompra)
        FROM Compra c
        JOIN c.sesion s
        JOIN s.pelicula p
        JOIN s.sala sa
        ORDER BY c.fechaCompra DESC
        """,
            countQuery = "SELECT COUNT(c) FROM Compra c")
    Page<CompraResumen> encuentraTodasLasCompras(Pageable pageable);

    @Query(value = """
        SELECT new com.safa.cinetown.dtos.CompraResumen(
               c.codCompra, p.titulo, sa.numero, s.fechaHoraInicio,
               c.numEntradas, c.precioTotal, c.estado, c.fechaCompra)
        FROM Compra c
        JOIN c.sesion s
        JOIN s.pelicula p
        JOIN s.sala sa
        WHERE c.usuario.id = :usuarioId
        ORDER BY c.fechaCompra DESC
        """, //La consulta del COUNT va aparte para que no le afecte el LIMIT de la paginación
            countQuery = "SELECT COUNT(c) FROM Compra c WHERE c.usuario.id = :usuarioId")
    Page<CompraResumen> encuentraComprasDeUsuario(@Param("usuarioId") Integer usuarioId, Pageable pageable);


}
