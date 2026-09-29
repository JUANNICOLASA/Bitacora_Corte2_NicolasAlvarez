package com.restaurante.validator;

import com.restaurante.exception.RecursoDuplicadoException;
import com.restaurante.exception.ReglaNegocioException;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.repository.CoctelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoctelValidatorTest {

    @Mock
    private CoctelRepository coctelRepository;

    @InjectMocks
    private CoctelValidator validator;

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
    void nombreDuplicadoAlCrearLanzaConflicto() {
        when(coctelRepository.existsByNombreIgnoreCase("NEGRONI")).thenReturn(true);

        assertThatThrownBy(() -> validator.validarNombreUnico("  NEGRONI ", null))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    void nombreNuevoAlCrearEsValido() {
        when(coctelRepository.existsByNombreIgnoreCase("Boulevardier")).thenReturn(false);

        assertThatCode(() -> validator.validarNombreUnico("Boulevardier", null)).doesNotThrowAnyException();
    }

    @Test
    void alActualizarSeExcluyeElMismoCoctel() {
        when(coctelRepository.existsByNombreIgnoreCaseAndIdNot("Negroni", 1L)).thenReturn(false);

        assertThatCode(() -> validator.validarNombreUnico("Negroni", 1L)).doesNotThrowAnyException();
    }

    @Test
    void alActualizarConNombreDeOtroCoctelLanzaConflicto() {
        when(coctelRepository.existsByNombreIgnoreCaseAndIdNot("", 2L)).thenReturn(true);

        assertThatThrownBy(() -> validator.validarNombreUnico(null, 2L))
                .isInstanceOf(RecursoDuplicadoException.class);
    }
}
