package com.restaurante.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Coctel de la carta. El atributo destiladoBase indica el tipo de licor
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coctel {

    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private CategoriaCoctel categoria;
    private TipoBebida tipo;
    private String destiladoBase;
    private Boolean disponible;

    public boolean esMocktail() {
        return tipo == TipoBebida.MOCKTAIL;
    }

    public boolean estaDisponible() {
        return Boolean.TRUE.equals(disponible);
    }
}
