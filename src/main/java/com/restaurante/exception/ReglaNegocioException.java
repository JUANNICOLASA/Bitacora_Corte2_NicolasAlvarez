package com.restaurante.exception;

import org.springframework.http.HttpStatus;

// 422 Unprocessable Entity: el formato esta bien pero el negocio lo rechaza (400 seria formato mal)
public class ReglaNegocioException extends BlueVelvetException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje, HttpStatus.UNPROCESSABLE_ENTITY, "BV-422");
    }
}
