package com.safa.cinetown.repositorios;

import com.safa.cinetown.dtos.RecaudacionPelicula;
import com.safa.cinetown.modelos.Pelicula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPeliculaRepository extends JpaRepository <Pelicula, String> {
    List<Pelicula> findByTituloContainingIgnoreCase(String titulo);

    List<Pelicula> findByActivoTrueOrderByTituloAsc();

    @Query("""
        SELECT new com.safa.cinetown.dtos.RecaudacionPelicula(
               p.id, p.titulo,
               CAST(COALESCE(SUM(c.numEntradas), 0) AS Long),
               CAST(COALESCE(SUM(c.precioTotal), 0) AS BigDecimal))
        FROM Pelicula p
        LEFT JOIN Sesion s ON s.pelicula = p
        LEFT JOIN Compra c ON c.sesion = s AND c.estado = true
        WHERE p.id = :peliculaId
        GROUP BY p.id, p.titulo
        """)
    Optional<RecaudacionPelicula> calcularRecaudacionPorPelicula(@Param("peliculaId") Integer peliculaId);
}