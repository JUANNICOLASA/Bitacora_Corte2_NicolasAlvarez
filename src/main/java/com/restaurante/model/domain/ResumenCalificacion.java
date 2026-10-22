package com.restaurante.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenCalificacion {

    private TipoProducto tipo;
    private Long idProducto;
    private Double promedio;
    private Long total;
    private Map<Integer, Long> distribucion;
}
