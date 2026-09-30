package com.safa.cinetown.repositorios;

import com.safa.cinetown.modelos.Pelicula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPeliculaRepository extends JpaRepository <Pelicula, String>{
}
