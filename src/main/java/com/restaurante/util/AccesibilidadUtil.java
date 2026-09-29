package com.restaurante.util;

public final class AccesibilidadUtil {

    public static final String ETIQUETA_AGOTADO = "AGOTADO EN BARRA";
    public static final String ETIQUETA_DISPONIBLE = "DISPONIBLE";
    public static final String ETIQUETA_BLOQUEADO_MOCKTAIL = "NO DISPONIBLE PARA MOCKTAIL";
    public static final String ICONO_CANDADO = "candado";
    public static final String ICONO_DISPONIBLE = "check";

    private AccesibilidadUtil() {
    }

    public static String etiquetaDisponibilidad(boolean disponible) {
        return disponible ? ETIQUETA_DISPONIBLE : ETIQUETA_AGOTADO;
    }

    public static String iconoDisponibilidad(boolean disponible) {
        return disponible ? ICONO_DISPONIBLE : ICONO_CANDADO;
    }

    public static String etiquetaModificador(boolean disponible, boolean bloqueadoPorMocktail) {
        if (!disponible) {
            return ETIQUETA_AGOTADO;
        }
        return bloqueadoPorMocktail ? ETIQUETA_BLOQUEADO_MOCKTAIL : ETIQUETA_DISPONIBLE;
    }
}
