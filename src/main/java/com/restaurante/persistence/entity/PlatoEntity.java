package com.restaurante.persistence.entity;

import com.restaurante.model.domain.CategoriaPlato;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plato", indexes = @Index(name = "idx_plato_categoria", columnList = "categoria"))
@Getter
@Setter
@NoArgsConstructor
public class PlatoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(length = 250)
    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaPlato categoria;

    @Column(length = 60)
    private String porcion;

    @Column(nullable = false)
    private Boolean disponible;
}
