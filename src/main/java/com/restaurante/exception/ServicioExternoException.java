package com.restaurante.exception;

import org.springframework.http.HttpStatus;

public class ServicioExternoException extends BlueVelvetException {

    public ServicioExternoException(String mensaje) {
        super(mensaje, HttpStatus.BAD_GATEWAY, "BV-502");
    }
}
