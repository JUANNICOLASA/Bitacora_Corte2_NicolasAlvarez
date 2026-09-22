package com.restaurante.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextoUtilTest {

    @Test
    void normalizarQuitaTildesEspaciosYMayusculas() {
        assertThat(TextoUtil.normalizar("  Piña   COLADÁ ")).isEqualTo("pina colada");
    }

    @Test
    void normalizarNullDevuelveVacio() {
        assertThat(TextoUtil.normalizar(null)).isEmpty();
    }

    @Test
    void sonIgualesIgnoraFormato() {
        assertThat(TextoUtil.sonIguales("Old Fashioned", "  old   fashioned")).isTrue();
        assertThat(TextoUtil.sonIguales("Negroni", "Boulevardier")).isFalse();
    }

    @Test
    void estaVacioDetectaNullYBlancos() {
        assertThat(TextoUtil.estaVacio(null)).isTrue();
        assertThat(TextoUtil.estaVacio("   ")).isTrue();
        assertThat(TextoUtil.estaVacio("Gin")).isFalse();
    }
}
