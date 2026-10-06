package com.safa.cinetown.repositorios;

import com.safa.cinetown.dtos.DetalleCompra;
import com.safa.cinetown.modelos.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ICompraRepository extends JpaRepository <Compra, Integer>{
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
}
