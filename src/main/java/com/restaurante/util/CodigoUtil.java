package com.restaurante.util;

import java.security.SecureRandom;

public final class CodigoUtil {

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LONGITUD = 6;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private CodigoUtil() {
    }

    public static String generarCodigoReserva() {
        StringBuilder codigo = new StringBuilder("BV-");
        for (int i = 0; i < LONGITUD; i++) {
            codigo.append(ALFABETO.charAt(ALEATORIO.nextInt(ALFABETO.length())));
        }
        return codigo.toString();
    }
}
