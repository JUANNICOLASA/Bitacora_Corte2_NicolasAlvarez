package com.restaurante.exception;

import org.springframework.http.HttpStatus;

public class RecursoDuplicadoException extends BlueVelvetException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT, "BV-409");
    }
}
