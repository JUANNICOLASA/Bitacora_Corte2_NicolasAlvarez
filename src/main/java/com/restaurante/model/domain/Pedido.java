package com.restaurante.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Comanda enviada al tablero del bartender.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    private Long id;
    private Integer numeroMesa;
    @Builder.Default
    private List<ItemPedido> items = new ArrayList<>();
    private EstadoPedido estado;
    private LocalDateTime fechaCreacion;

    public boolean puedeModificarse() {
        return estado == EstadoPedido.RECIBIDO;
    }

    public boolean estaActivo() {
        return estado != null && !estado.esFinal();
    }

    public double calcularTotal() {
        return items == null ? 0.0 : items.stream().mapToDouble(ItemPedido::subtotal).sum();
    }
}
