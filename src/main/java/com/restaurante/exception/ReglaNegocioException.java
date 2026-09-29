package com.restaurante.exception;

import org.springframework.http.HttpStatus;

public class ReglaNegocioException extends BlueVelvetException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje, HttpStatus.UNPROCESSABLE_ENTITY, "BV-422");
    }
}
