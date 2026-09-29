package com.safa.cinetown.modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.*;

@Entity
@Table(name = "genero", schema = "cine")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Genero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, columnDefinition = "nombre", length=100)
    private String nombre;

    @ManyToMany(mappedBy = "generos")
    private Set<Pelicula> peliculas = new HashSet<>();
}
