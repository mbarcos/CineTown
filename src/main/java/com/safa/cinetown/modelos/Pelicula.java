package com.safa.cinetown.modelos;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String sinopsis;

    @Column(nullable = false)
    private Integer duracion;

    @Column(nullable = false)
    private Integer anio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "cine.clasificacion_edad")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ClasificacionEdad clasificacion;

    @Column(nullable = false)
    private Boolean activo = true;
}
