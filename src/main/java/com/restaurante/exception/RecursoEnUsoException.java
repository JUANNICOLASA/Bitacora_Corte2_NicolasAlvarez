package com.restaurante.exception;

import org.springframework.http.HttpStatus;

public class RecursoEnUsoException extends BlueVelvetException {

    public RecursoEnUsoException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT, "BV-409");
    }
}
