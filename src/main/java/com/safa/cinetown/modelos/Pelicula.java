package com.safa.cinetown.modelos;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.*;


@Entity
@Table(name = "pelicula", schema = "cine")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pelicula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, columnDefinition = "titulo")
    private String titulo;

    @Column(columnDefinition = "sinopsis")
    private String sinopsis;

    @Column(nullable = false, columnDefinition = "duracion")
    private Integer duracion;

    @Column(nullable = false, columnDefinition = "anio")
    private Integer anio;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "clasificacion_edad")
    private ClasificacionEdad clasificacion;

    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "pelicula_genero",
            schema = "cine",
            joinColumns = @JoinColumn(name = "pelicula_id"),
            inverseJoinColumns = @JoinColumn(name = "genero_id")
    )
    private Set<Genero> generos = new HashSet<>();
}
