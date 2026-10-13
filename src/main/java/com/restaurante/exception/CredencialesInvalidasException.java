package com.restaurante.exception;

import org.springframework.http.HttpStatus;

// 401: mensaje generico a proposito para no revelar si el correo existe o no
public class CredencialesInvalidasException extends BlueVelvetException {

    public CredencialesInvalidasException() {
        super("Correo o contrasena incorrectos", HttpStatus.UNAUTHORIZED, "BV-401");
    }
}
