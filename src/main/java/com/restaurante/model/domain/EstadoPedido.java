package com.restaurante.model.domain;

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
