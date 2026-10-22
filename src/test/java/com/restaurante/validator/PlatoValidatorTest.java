package com.restaurante.validator;

import com.restaurante.exception.RecursoDuplicadoException;
import com.restaurante.repository.PlatoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatoValidatorTest {

    @Mock
    private PlatoRepository platoRepository;

    @InjectMocks
    private PlatoValidator platoValidator;

    @Test
    void nombreNuevoEsValido() {
        when(platoRepository.existsByNombreIgnoreCase("Papas trufadas")).thenReturn(false);

        assertThatCode(() -> platoValidator.validarNombreUnico(" Papas trufadas ", null)).doesNotThrowAnyException();
    }

    @Test
    void nombreRepetidoAlCrearOActualizar() {
        when(platoRepository.existsByNombreIgnoreCase("")).thenReturn(true);
        when(platoRepository.existsByNombreIgnoreCaseAndIdNot("Papas trufadas", 2L)).thenReturn(true);

        assertThatThrownBy(() -> platoValidator.validarNombreUnico(null, null))
                .isInstanceOf(RecursoDuplicadoException.class);
        assertThatThrownBy(() -> platoValidator.validarNombreUnico("Papas trufadas", 2L))
                .isInstanceOf(RecursoDuplicadoException.class);
    }
}
