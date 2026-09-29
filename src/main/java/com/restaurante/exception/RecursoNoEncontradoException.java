package com.restaurante.exception;

import org.springframework.http.HttpStatus;

public class RecursoNoEncontradoException extends BlueVelvetException {

    public RecursoNoEncontradoException(String recurso, Object id) {
        super(recurso + " con id " + id + " no existe", HttpStatus.NOT_FOUND, "BV-404");
    }
}
