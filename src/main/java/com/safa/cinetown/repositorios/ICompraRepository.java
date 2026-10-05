package com.safa.cinetown.repositorios;

import com.safa.cinetown.modelos.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICompraRepository extends JpaRepository <Compra, Integer>{

}
