package com.safa.cinetown.repositorios;

import com.safa.cinetown.modelos.Genero;
import com.safa.cinetown.modelos.Pelicula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IGeneroRepository extends JpaRepository <Genero, String> {
    @Query("""
            SELECT genero.nombre, COUNT(peliculas)
            FROM Genero genero
            LEFT JOIN genero.peliculas peliculas ON peliculas.activo = true
            GROUP BY genero.id, genero.nombre
            ORDER BY genero.nombre
            """)
    List<Object[]> contarPeliculasEnCarteleraPorGenero();

}
