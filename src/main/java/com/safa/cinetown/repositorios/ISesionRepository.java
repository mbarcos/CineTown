package com.safa.cinetown.repositorios;

import com.safa.cinetown.dtos.OcupacionPorSesion;
import com.safa.cinetown.modelos.Sesion;
import com.safa.cinetown.dtos.SesionConAsientos;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface ISesionRepository extends JpaRepository<Sesion, Integer> {
    @Query("""
            SELECT new com.safa.cinetown.dtos.SesionConAsientos(
                   s.id, p.titulo, sa.numero, s.fechaHoraInicio, s.fechaHoraFin,
                   s.precioBase, sa.capacidad,
                   CAST(
                   sa.capacidad - COALESCE(SUM(c.numEntradas), 0L) AS Integer)
                   )
            FROM Sesion s
            JOIN s.pelicula p
            JOIN s.sala sa
            LEFT JOIN Compra c ON c.sesion = s AND c.estado = true
            WHERE s.fechaHoraInicio > CURRENT_TIMESTAMP
            GROUP BY s.id, p.titulo, sa.numero, s.fechaHoraInicio,
                     s.fechaHoraFin, s.precioBase, sa.capacidad
            ORDER BY s.fechaHoraInicio
            """)
     List<SesionConAsientos> encuentraSesionesActivasConAsientosLibres();

    @EntityGraph(attributePaths = "pelicula")
    List<Sesion> findByPeliculaIdOrderByFechaHoraInicioAsc(Integer peliculaId);

    @Query("""
        SELECT new com.safa.cinetown.dtos.OcupacionPorSesion(
               s.id, p.titulo, sa.numero, s.fechaHoraInicio,
               sa.capacidad,
               CAST(COALESCE(SUM(c.numEntradas), 0) AS Integer))
        FROM Sesion s
        JOIN s.pelicula p
        JOIN s.sala sa
        LEFT JOIN Compra c ON c.sesion = s AND c.estado = true
        WHERE s.fechaHoraInicio >= :inicio
          AND s.fechaHoraInicio < :fin
        GROUP BY s.id, p.titulo, sa.numero, s.fechaHoraInicio, sa.capacidad
        ORDER BY s.fechaHoraInicio
        """)
    List<OcupacionPorSesion> calcularOcupacionPorSesion(@Param("inicio") LocalDateTime inicio,
                                                        @Param("fin") LocalDateTime fin);
}
