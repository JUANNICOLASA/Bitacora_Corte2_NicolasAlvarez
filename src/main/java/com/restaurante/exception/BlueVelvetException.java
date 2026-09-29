package com.restaurante.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

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
