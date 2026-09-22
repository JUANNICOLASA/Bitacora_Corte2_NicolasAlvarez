package com.restaurante.model.domain;

/**
 * Estados de una comanda en el tablero del bartender (KDS).
 * Flujo: RECIBIDO -> EN_PREPARACION -> LISTO -> ENTREGADO. Solo se cancela en RECIBIDO.
 */
public enum EstadoPedido {
    RECIBIDO,
    EN_PREPARACION,
    LISTO,
    ENTREGADO,
    CANCELADO;

    /**
     * Devuelve el estado que sigue en el flujo normal, o null si es un estado final.
     */
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
