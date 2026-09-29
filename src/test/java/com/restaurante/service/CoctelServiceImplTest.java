package com.restaurante.service;

import com.restaurante.exception.RecursoDuplicadoException;
import com.restaurante.exception.RecursoEnUsoException;
import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.CoctelEntityMapper;
import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.persistence.entity.CoctelEntity;
import com.restaurante.repository.CoctelRepository;
import com.restaurante.repository.PedidoRepository;
import com.restaurante.validator.CoctelValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoctelServiceImplTest {

    @Mock
    private CoctelRepository coctelRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private CoctelValidator coctelValidator;

    @Spy
    private CoctelEntityMapper coctelEntityMapper = Mappers.getMapper(CoctelEntityMapper.class);

    @InjectMocks
    private CoctelServiceImpl coctelService;

    private Coctel coctel(String nombre, Boolean disponible) {
        return Coctel.builder()
                .nombre(nombre)
                .precio(38000.0)
                .categoria(CategoriaCoctel.CLASICO)
                .tipo(TipoBebida.ALCOHOLICA)
                .destiladoBase("Gin")
                .disponible(disponible)
                .build();
    }

    private CoctelEntity entity(Long id, String nombre, boolean disponible) {
        CoctelEntity entity = new CoctelEntity();
        entity.setId(id);
        entity.setNombre(nombre);
        entity.setPrecio(38000.0);
        entity.setCategoria(CategoriaCoctel.CLASICO);
        entity.setTipo(TipoBebida.ALCOHOLICA);
        entity.setDestiladoBase("Gin");
        entity.setDisponible(disponible);
        return entity;
    }

    @Test
    void crearGuardaEnBaseDeDatosYDevuelveElId() {
        when(coctelRepository.save(any(CoctelEntity.class))).thenAnswer(invocacion -> {
            CoctelEntity guardado = invocacion.getArgument(0);
            guardado.setId(1L);
            return guardado;
        });

        Coctel creado = coctelService.crear(coctel("  Negroni ", null));

        assertThat(creado.getId()).isEqualTo(1L);
        assertThat(creado.getNombre()).isEqualTo("Negroni");
        assertThat(creado.getDisponible()).isTrue();
        verify(coctelValidator).validarNombreUnico(any(), isNull());
    }

    @Test
    void crearConNombreDuplicadoNoGuarda() {
        doThrow(new RecursoDuplicadoException("duplicado"))
                .when(coctelValidator).validarNombreUnico(any(), isNull());

        Coctel nuevo = coctel("Negroni", true);
        assertThatThrownBy(() -> coctelService.crear(nuevo))
                .isInstanceOf(RecursoDuplicadoException.class);
        verify(coctelRepository, never()).save(any());
    }

    @Test
    void listarVacioDevuelveListaVacia() {
        when(coctelRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

        assertThat(coctelService.listar()).isEmpty();
    }

    @Test
    void listarConvierteEntidadesADominio() {
        when(coctelRepository.findAllByOrderByIdAsc())
                .thenReturn(List.of(entity(1L, "Negroni", true), entity(2L, "Pina Colada", false)));
        when(coctelRepository.findByCategoriaOrderByIdAsc(CategoriaCoctel.CLASICO))
                .thenReturn(List.of(entity(1L, "Negroni", true)));
        when(coctelRepository.findByDisponibleTrueOrderByIdAsc())
                .thenReturn(List.of(entity(1L, "Negroni", true)));

        assertThat(coctelService.listar()).extracting(Coctel::getNombre).containsExactly("Negroni", "Pina Colada");
        assertThat(coctelService.listarPorCategoria(CategoriaCoctel.CLASICO)).hasSize(1);
        assertThat(coctelService.obtenerDisponibles()).extracting(Coctel::getId).containsExactly(1L);
    }

    @Test
    void obtenerPorIdInexistenteLanzaNoEncontrado() {
        when(coctelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coctelService.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void obtenerPorIdExistente() {
        when(coctelRepository.findById(1L)).thenReturn(Optional.of(entity(1L, "Negroni", true)));

        assertThat(coctelService.obtenerPorId(1L).getNombre()).isEqualTo("Negroni");
    }

    @Test
    void actualizarModificaLaEntidadYConservaDisponibilidad() {
        CoctelEntity existente = entity(1L, "Negroni", false);
        when(coctelRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(coctelRepository.save(existente)).thenReturn(existente);

        Coctel actualizado = coctelService.actualizar(1L, coctel("Negroni Sbagliato", null));

        assertThat(actualizado.getId()).isEqualTo(1L);
        assertThat(actualizado.getNombre()).isEqualTo("Negroni Sbagliato");
        assertThat(actualizado.getDisponible()).isFalse();
        verify(coctelValidator).validarNombreUnico("Negroni Sbagliato", 1L);
    }

    @Test
    void actualizarInexistenteLanzaNoEncontrado() {
        when(coctelRepository.findById(5L)).thenReturn(Optional.empty());

        Coctel cambios = coctel("Negroni", true);
        assertThatThrownBy(() -> coctelService.actualizar(5L, cambios))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void cambiarDisponibilidadMarcaAgotado() {
        CoctelEntity existente = entity(1L, "Negroni", true);
        when(coctelRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(coctelRepository.save(existente)).thenReturn(existente);

        Coctel resultado = coctelService.cambiarDisponibilidad(1L, false);

        assertThat(resultado.getDisponible()).isFalse();
        ArgumentCaptor<CoctelEntity> captor = ArgumentCaptor.forClass(CoctelEntity.class);
        verify(coctelRepository).save(captor.capture());
        assertThat(captor.getValue().getDisponible()).isFalse();
    }

    @Test
    void eliminarSinComandasBorraElRegistro() {
        when(coctelRepository.findById(1L)).thenReturn(Optional.of(entity(1L, "Negroni", true)));
        when(pedidoRepository.existsByItemsCoctelId(1L)).thenReturn(false);

        coctelService.eliminar(1L);

        verify(coctelRepository).deleteById(1L);
    }

    @Test
    void eliminarConComandasLanzaConflicto() {
        when(coctelRepository.findById(1L)).thenReturn(Optional.of(entity(1L, "Negroni", true)));
        when(pedidoRepository.existsByItemsCoctelId(1L)).thenReturn(true);

        assertThatThrownBy(() -> coctelService.eliminar(1L))
                .isInstanceOf(RecursoEnUsoException.class);
        verify(coctelRepository, never()).deleteById(anyLong());
    }

    @Test
    void eliminarInexistenteLanzaNoEncontrado() {
        when(coctelRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coctelService.eliminar(1L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
