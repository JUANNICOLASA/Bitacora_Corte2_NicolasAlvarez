package com.restaurante.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccesibilidadUtilTest {

    @Test
    void agotadoTieneEtiquetaEnMayusculasYCandado() {
        assertThat(AccesibilidadUtil.etiquetaDisponibilidad(false)).isEqualTo("AGOTADO EN BARRA");
        assertThat(AccesibilidadUtil.iconoDisponibilidad(false)).isEqualTo("candado");
    }

    @Test
    void disponibleTieneEtiquetaYCheck() {
        assertThat(AccesibilidadUtil.etiquetaDisponibilidad(true)).isEqualTo("DISPONIBLE");
        assertThat(AccesibilidadUtil.iconoDisponibilidad(true)).isEqualTo("check");
    }

    @Test
    void etiquetaModificadorSegunEstado() {
        assertThat(AccesibilidadUtil.etiquetaModificador(false, false)).isEqualTo("AGOTADO EN BARRA");
        assertThat(AccesibilidadUtil.etiquetaModificador(false, true)).isEqualTo("AGOTADO EN BARRA");
        assertThat(AccesibilidadUtil.etiquetaModificador(true, true)).isEqualTo("NO DISPONIBLE PARA MOCKTAIL");
        assertThat(AccesibilidadUtil.etiquetaModificador(true, false)).isEqualTo("DISPONIBLE");
    }
}
