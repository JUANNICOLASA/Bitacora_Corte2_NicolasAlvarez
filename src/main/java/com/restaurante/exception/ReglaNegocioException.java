package com.restaurante.exception;

import org.springframework.http.HttpStatus;

/**
 * 422 - La peticion tiene buen formato pero viola una regla de negocio de Blue Velvet.
 */
public class ReglaNegocioException extends BlueVelvetException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje, HttpStatus.UNPROCESSABLE_ENTITY, "BV-422");
    }
}
