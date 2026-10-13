package com.restaurante.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// S07 - Entidad de negocio (objeto de dominio): tiene datos Y comportamiento (esMocktail, estaDisponible).
// No depende de como se guarda (no tiene @Entity ni @Column). Vive en model/domain.
// S08 - Lombok: @Data (getters, setters, equals, hashCode, toString), @Builder y constructores.
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
