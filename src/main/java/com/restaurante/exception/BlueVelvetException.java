package com.restaurante.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepcion base del negocio. Cada subclase define su codigo HTTP y un codigo interno.
 */
@Getter
public abstract class BlueVelvetException extends RuntimeException {

    private final HttpStatus status;
    private final String codigo;

    protected BlueVelvetException(String mensaje, HttpStatus status, String codigo) {
        super(mensaje);
        this.status = status;
        this.codigo = codigo;
    }
}
