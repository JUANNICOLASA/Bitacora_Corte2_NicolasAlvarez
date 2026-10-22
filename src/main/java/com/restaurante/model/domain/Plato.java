package com.restaurante.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plato {

    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private CategoriaPlato categoria;
    private String porcion;
    private Boolean disponible;

    public boolean estaDisponible() {
        return Boolean.TRUE.equals(disponible);
    }
}
