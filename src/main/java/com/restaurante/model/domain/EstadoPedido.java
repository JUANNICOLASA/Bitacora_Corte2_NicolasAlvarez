package com.restaurante.model.domain;

// S07 - Enumeracion: conjunto fijo de constantes (estados, roles, categorias).
// Aqui ademas define el flujo del pedido en el KDS.
public enum EstadoPedido {
    RECIBIDO,
    EN_PREPARACION,
    LISTO,
    ENTREGADO,
    CANCELADO;

    public EstadoPedido siguiente() {
        return switch (this) {
            case RECIBIDO -> EN_PREPARACION;
            case EN_PREPARACION -> LISTO;
            case LISTO -> ENTREGADO;
            case ENTREGADO, CANCELADO -> null;
        };
    }

    public boolean esFinal() {
        return this == ENTREGADO || this == CANCELADO;
    }
}
