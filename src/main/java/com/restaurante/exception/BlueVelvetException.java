package com.restaurante.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// S08 - Excepcion base propia. Extiende RuntimeException (no obliga a try/catch) y lleva el status HTTP
// y un codigo interno (BV-xxx) para que el GlobalExceptionHandler arme la respuesta uniforme.
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
