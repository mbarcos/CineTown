package com.safa.cinetown.repositorios;

import com.safa.cinetown.modelos.Compra;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICompraRepository extends JpaRepository <Compra, Integer>{
    @EntityGraph(attributePaths = {"sesion", "sesion.pelicula", "sesion.sala"})
    Optional<Compra> findByCodCompra(String codCompra);

}
