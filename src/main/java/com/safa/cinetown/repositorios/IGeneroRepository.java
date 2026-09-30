package com.safa.cinetown.repositorios;

import com.safa.cinetown.modelos.Genero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IGeneroRepository extends JpaRepository <Genero, String>{

}
