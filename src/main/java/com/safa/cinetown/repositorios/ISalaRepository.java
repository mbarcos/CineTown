package com.safa.cinetown.repositorios;

import com.safa.cinetown.modelos.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISalaRepository extends JpaRepository<Sala, Integer> {
    List<Sala> findAllByOrderByNumeroAsc();

}
