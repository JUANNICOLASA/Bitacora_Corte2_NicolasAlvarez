package com.restaurante.validator;

import com.restaurante.exception.RecursoDuplicadoException;
import com.restaurante.exception.ReglaNegocioException;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.TipoBebida;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoctelValidatorTest {

    private final CoctelValidator validator = new CoctelValidator();

    @Test
    void alcoholicoConDestiladoEsValido() {
        Coctel negroni = Coctel.builder().tipo(TipoBebida.ALCOHOLICA).destiladoBase("Gin").build();
        assertThatCode(() -> validator.validarCoctel(negroni)).doesNotThrowAnyException();
    }

    @Test
    void mocktailSinDestiladoEsValido() {
        Coctel mojito = Coctel.builder().tipo(TipoBebida.MOCKTAIL).destiladoBase(null).build();
        assertThatCode(() -> validator.validarCoctel(mojito)).doesNotThrowAnyException();
    }

    @Test
    void alcoholicoSinDestiladoLanzaReglaNegocio() {
        Coctel coctel = Coctel.builder().tipo(TipoBebida.ALCOHOLICA).destiladoBase(" ").build();
        assertThatThrownBy(() -> validator.validarCoctel(coctel))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("destilado base");
    }

    @Test
    void mocktailConDestiladoLanzaReglaNegocio() {
        Coctel coctel = Coctel.builder().tipo(TipoBebida.MOCKTAIL).destiladoBase("Ron").build();
        assertThatThrownBy(() -> validator.validarCoctel(coctel))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Mocktail");
    }

    @Test
    void nombreDuplicadoLanzaConflicto() {
        List<Coctel> existentes = List.of(Coctel.builder().id(1L).nombre("Negroni").build());
        assertThatThrownBy(() -> validator.validarNombreUnico("  NEGRONI ", existentes, null))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    void mismoNombreDelMismoCoctelAlActualizarEsValido() {
        List<Coctel> existentes = List.of(Coctel.builder().id(1L).nombre("Negroni").build());
        assertThatCode(() -> validator.validarNombreUnico("Negroni", existentes, 1L))
                .doesNotThrowAnyException();
    }
}
