package com.restaurante.util;

import java.text.Normalizer;
import java.util.Locale;

public final class TextoUtil {

    private TextoUtil() {
    }

    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    public static boolean sonIguales(String a, String b) {
        return normalizar(a).equals(normalizar(b));
    }

    public static boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
