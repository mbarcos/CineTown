package com.safa.cinetown.repositorios;

import com.safa.cinetown.modelos.Sesion;
import com.safa.cinetown.dtos.SesionConAsientos;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ISesionRepository extends JpaRepository<Sesion, Integer> {
    @Query("""
            SELECT new com.safa.cinetown.dtos.SesionConAsientos(
                   s.id, p.titulo, sa.numero, s.fechaHoraInicio, s.fechaHoraFin,
                   s.precioBase, sa.capacidad,
                   sa.capacidad - COALESCE(SUM(c.numEntradas), 0L))
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
}
