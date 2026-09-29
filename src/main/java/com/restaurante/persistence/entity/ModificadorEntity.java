package com.restaurante.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "modificador")
@Getter
@Setter
@NoArgsConstructor
public class ModificadorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(name = "graduacion_alcoholica", nullable = false)
    private Double graduacionAlcoholica;

    @Column(name = "precio_extra", nullable = false)
    private Double precioExtra;

    @Column(nullable = false)
    private Boolean disponible;
}
