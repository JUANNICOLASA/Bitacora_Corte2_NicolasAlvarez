package com.restaurante.exception;

import org.springframework.http.HttpStatus;

// 429 Too Many Requests (S10 - proteccion contra fuerza bruta en el login)
public class DemasiadosIntentosException extends BlueVelvetException {

    public DemasiadosIntentosException(long minutos) {
        super("Demasiados intentos fallidos. Intente de nuevo en " + minutos + " minutos",
                HttpStatus.TOO_MANY_REQUESTS, "BV-429");
    }
}
