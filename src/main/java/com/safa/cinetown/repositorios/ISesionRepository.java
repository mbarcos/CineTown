package com.safa.cinetown.repositorios;

import com.safa.cinetown.modelos.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISesionRepository extends JpaRepository<Sesion, Integer> {

}
