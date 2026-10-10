package com.restaurante.exception;

import org.springframework.http.HttpStatus;

public class DemasiadosIntentosException extends BlueVelvetException {

    public DemasiadosIntentosException(long minutos) {
        super("Demasiados intentos fallidos. Intente de nuevo en " + minutos + " minutos",
                HttpStatus.TOO_MANY_REQUESTS, "BV-429");
    }
}
