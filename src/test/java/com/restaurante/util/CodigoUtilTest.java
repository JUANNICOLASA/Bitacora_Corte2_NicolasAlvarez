package com.restaurante.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class CodigoUtilTest {

    @Test
    void generaCodigosConFormatoYSinCaracteresAmbiguos() {
        Set<String> codigos = new HashSet<>();
        IntStream.range(0, 200).forEach(i -> codigos.add(CodigoUtil.generarCodigoReserva()));

        assertThat(codigos).hasSizeGreaterThan(195)
                .allMatch(codigo -> codigo.matches("BV-[A-HJ-NP-Z2-9]{6}"));
    }
}
