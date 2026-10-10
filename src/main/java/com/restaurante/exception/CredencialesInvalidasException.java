package com.restaurante.exception;

import org.springframework.http.HttpStatus;

public class CredencialesInvalidasException extends BlueVelvetException {

    public CredencialesInvalidasException() {
        super("Correo o contrasena incorrectos", HttpStatus.UNAUTHORIZED, "BV-401");
    }
}
