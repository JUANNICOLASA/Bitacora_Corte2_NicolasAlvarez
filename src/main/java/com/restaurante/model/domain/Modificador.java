package com.restaurante.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ingrediente o adicion que personaliza un coctel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Modificador {

    private Long id;
    private String nombre;
    private Double graduacionAlcoholica;
    private Double precioExtra;
    private Boolean disponible;

    public boolean esAlcoholico() {
        return graduacionAlcoholica != null && graduacionAlcoholica > 0;
    }

    public boolean estaDisponible() {
        return Boolean.TRUE.equals(disponible);
    }

    /**
     * Un modificador es compatible si esta disponible y no lleva alcohol cuando el coctel es Mocktail.
     */
    public boolean esCompatibleCon(Coctel coctel) {
        return estaDisponible() && !(coctel.esMocktail() && esAlcoholico());
    }
}
