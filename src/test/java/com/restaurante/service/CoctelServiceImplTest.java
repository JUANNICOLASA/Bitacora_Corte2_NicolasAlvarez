package com.restaurante.service;

import com.restaurante.exception.RecursoDuplicadoException;
import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.validator.CoctelValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CoctelServiceImplTest {

    @Mock
    private CoctelValidator coctelValidator;

    @InjectMocks
    private CoctelServiceImpl coctelService;

    private Coctel coctel(String nombre, CategoriaCoctel categoria, Boolean disponible) {
        return Coctel.builder()
                .nombre(nombre)
                .precio(38000.0)
                .categoria(categoria)
                .tipo(TipoBebida.ALCOHOLICA)
                .destiladoBase("Gin")
                .disponible(disponible)
                .build();
    }

    @Test
    void crearAsignaIdYDisponiblePorDefecto() {
        Coctel creado = coctelService.crear(coctel("Negroni", CategoriaCoctel.CLASICO, null));

        assertThat(creado.getId()).isEqualTo(1L);
        assertThat(creado.getDisponible()).isTrue();
        verify(coctelValidator, times(1)).validarCoctel(creado);
        verify(coctelValidator, times(1)).validarNombreUnico(eq("Negroni"), anyCollection(), isNull());
    }

    @Test
    void crearConNombreDuplicadoPropagaConflicto() {
        doThrow(new RecursoDuplicadoException("duplicado"))
                .when(coctelValidator).validarNombreUnico(any(), anyCollection(), any());

        Coctel nuevo = coctel("Negroni", CategoriaCoctel.CLASICO, true);
        assertThatThrownBy(() -> coctelService.crear(nuevo))
                .isInstanceOf(RecursoDuplicadoException.class);
        assertThat(coctelService.listar()).isEmpty();
    }

    @Test
    void listarVacioDevuelveListaVacia() {
        assertThat(coctelService.listar()).isEmpty();
    }

    @Test
    void listarFiltraPorCategoriaYDisponibles() {
        coctelService.crear(coctel("Negroni", CategoriaCoctel.CLASICO, true));
        coctelService.crear(coctel("Whisky Sour", CategoriaCoctel.SOUR, true));
        coctelService.crear(coctel("Pina Colada", CategoriaCoctel.TROPICAL, false));

        assertThat(coctelService.listar()).hasSize(3);
        assertThat(coctelService.listarPorCategoria(CategoriaCoctel.SOUR))
                .extracting(Coctel::getNombre).containsExactly("Whisky Sour");
        assertThat(coctelService.obtenerDisponibles())
                .extracting(Coctel::getNombre).containsExactly("Negroni", "Whisky Sour");
    }

    @Test
    void obtenerPorIdInexistenteLanzaNoEncontrado() {
        assertThatThrownBy(() -> coctelService.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void actualizarConservaIdYDisponibilidad() {
        Coctel creado = coctelService.crear(coctel("Negroni", CategoriaCoctel.CLASICO, false));

        Coctel cambios = coctel("Negroni Sbagliato", CategoriaCoctel.DE_AUTOR, null);
        Coctel actualizado = coctelService.actualizar(creado.getId(), cambios);

        assertThat(actualizado.getId()).isEqualTo(creado.getId());
        assertThat(actualizado.getNombre()).isEqualTo("Negroni Sbagliato");
        assertThat(actualizado.getDisponible()).isFalse();
        verify(coctelValidator).validarNombreUnico(eq("Negroni Sbagliato"), anyCollection(), eq(creado.getId()));
    }

    @Test
    void actualizarInexistenteLanzaNoEncontrado() {
        Coctel cambios = coctel("Negroni", CategoriaCoctel.CLASICO, true);
        assertThatThrownBy(() -> coctelService.actualizar(5L, cambios))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void cambiarDisponibilidadMarcaAgotado() {
        Coctel creado = coctelService.crear(coctel("Negroni", CategoriaCoctel.CLASICO, true));

        Coctel resultado = coctelService.cambiarDisponibilidad(creado.getId(), false);

        assertThat(resultado.getDisponible()).isFalse();
        assertThat(coctelService.obtenerDisponibles()).isEmpty();
    }

    @Test
    void eliminarQuitaElCoctel() {
        Coctel creado = coctelService.crear(coctel("Negroni", CategoriaCoctel.CLASICO, true));

        coctelService.eliminar(creado.getId());

        assertThat(coctelService.listar()).isEmpty();
    }

    @Test
    void eliminarInexistenteLanzaNoEncontrado() {
        assertThatThrownBy(() -> coctelService.eliminar(1L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
