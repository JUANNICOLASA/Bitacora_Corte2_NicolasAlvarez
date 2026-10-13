package com.restaurante.exception;

import org.springframework.http.HttpStatus;

// 409 Conflict: choca con un dato que ya existe
public class RecursoDuplicadoException extends BlueVelvetException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT, "BV-409");
    }
}
