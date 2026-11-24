package com.safa.cinetown.repositorios;

import com.safa.cinetown.dtos.RecaudacionPelicula;
import com.safa.cinetown.modelos.Pelicula;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPeliculaRepository extends JpaRepository <Pelicula, Integer> {
    List<Pelicula> findByTituloContainingIgnoreCase(String titulo);

    @Query("SELECT DISTINCT p FROM Pelicula p LEFT JOIN FETCH p.generos WHERE p.activo = true ORDER BY p.titulo ASC")
    List<Pelicula> findByActivoTrueOrderByTituloAsc();

    @Query("SELECT DISTINCT p FROM Pelicula p LEFT JOIN FETCH p.generos")
    List<Pelicula> findAllConGeneros();

    @Query("SELECT DISTINCT p FROM Pelicula p LEFT JOIN FETCH p.generos WHERE p.id = :id")
    Optional<Pelicula> findByIdConGeneros(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pelicula p WHERE p.id = :id")
    Optional<Pelicula> findByIdParaCambiarCartelera(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Pelicula p SET p.cartelUrl = :cartelUrl
        WHERE p.id = :id AND p.activo = true AND NULLIF(TRIM(p.cartelUrl), '') IS NULL
          AND p.titulo = :titulo AND p.anio = :anio
          AND p.sinopsis IS NOT DISTINCT FROM :sinopsis
        """)
    void guardarCartelSiFalta(@Param("id") Integer id, @Param("titulo") String titulo,
                             @Param("anio") Integer anio, @Param("sinopsis") String sinopsis,
                             @Param("cartelUrl") String cartelUrl);

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
