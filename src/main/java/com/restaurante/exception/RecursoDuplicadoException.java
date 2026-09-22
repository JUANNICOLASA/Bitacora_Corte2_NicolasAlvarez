package com.restaurante.exception;

import org.springframework.http.HttpStatus;

/**
 * 409 - Ya existe un recurso con el mismo identificador de negocio
 */
public class RecursoDuplicadoException extends BlueVelvetException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT, "BV-409");
    }
}
