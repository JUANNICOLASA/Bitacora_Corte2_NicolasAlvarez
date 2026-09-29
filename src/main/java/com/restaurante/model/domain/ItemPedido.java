package com.restaurante.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedido {

    private Long id;
    private Long idCoctel;
    private String nombreCoctel;
    private TipoBebida tipo;
    private Double precioUnitario;
    private Integer cantidad;
    private String destilado;
    @Builder.Default
    private List<Modificador> modificadores = new ArrayList<>();
    @Builder.Default
    private Integer cambiosDeLicor = 0;

    public double subtotal() {
        double extras = modificadores == null ? 0.0 : modificadores.stream()
                .mapToDouble(m -> m.getPrecioExtra() == null ? 0.0 : m.getPrecioExtra())
                .sum();
        double precio = precioUnitario == null ? 0.0 : precioUnitario;
        int unidades = cantidad == null ? 0 : cantidad;
        return (precio + extras) * unidades;
    }
}
