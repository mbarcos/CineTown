package com.safa.cinetown.repositorios;

import com.safa.cinetown.modelos.Sesion;
import com.safa.cinetown.proyecciones.SesionConAsientos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ISesionRepository extends JpaRepository<Sesion, Integer> {
    @Query("""
            SELECT s.id AS id,
                   p.titulo AS titulo,
                   sa.numero AS numeroSala,
                   s.fechaHoraInicio AS fechaHoraInicio,
                   s.fechaHoraFin AS fechaHoraFin,
                   s.precioBase AS precioBase,
                   sa.capacidad AS capacidad,
                   sa.capacidad - COALESCE(SUM(c.numEntradas), 0L) AS asientosLibres
            FROM Sesion s
            JOIN s.pelicula p
            JOIN s.sala sa
            LEFT JOIN Compra c ON c.sesion = s AND c.estado = true
            WHERE s.fechaHoraInicio > CURRENT_TIMESTAMP
            GROUP BY s.id, p.titulo, sa.numero, s.fechaHoraInicio,
                     s.fechaHoraFin, s.precioBase, sa.capacidad
            ORDER BY s.fechaHoraInicio
            """)
    List<SesionConAsientos> encontrarSesionesActivasConAsientosLibres();
}
