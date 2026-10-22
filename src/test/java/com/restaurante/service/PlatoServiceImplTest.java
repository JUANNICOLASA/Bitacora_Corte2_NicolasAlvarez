package com.restaurante.service;

import com.restaurante.exception.RecursoDuplicadoException;
import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.PlatoEntityMapper;
import com.restaurante.model.domain.CategoriaPlato;
import com.restaurante.model.domain.Plato;
import com.restaurante.persistence.entity.PlatoEntity;
import com.restaurante.repository.PlatoRepository;
import com.restaurante.validator.PlatoValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatoServiceImplTest {

    @Mock
    private PlatoRepository platoRepository;

    @Mock
    private PlatoValidator platoValidator;

    @Spy
    private PlatoEntityMapper platoEntityMapper = Mappers.getMapper(PlatoEntityMapper.class);

    @InjectMocks
    private PlatoServiceImpl platoService;

    private Plato plato(String nombre, Boolean disponible) {
        return Plato.builder().nombre(nombre).descripcion("Papas rusticas y trufa").precio(22000.0)
                .categoria(CategoriaPlato.PARA_COMPARTIR).porcion("Para compartir").disponible(disponible).build();
    }

    private PlatoEntity entity(Long id, boolean disponible) {
        PlatoEntity entity = platoEntityMapper.toEntity(plato("Papas trufadas", disponible));
        entity.setId(id);
        return entity;
    }

    @Test
    void crearGuardaConDisponibilidadPorDefecto() {
        when(platoRepository.save(any(PlatoEntity.class))).thenAnswer(invocacion -> {
            PlatoEntity guardado = invocacion.getArgument(0);
            guardado.setId(4L);
            return guardado;
        });

        Plato creado = platoService.crear(plato("  Papas trufadas ", null));

        assertThat(creado.getId()).isEqualTo(4L);
        assertThat(creado.getNombre()).isEqualTo("Papas trufadas");
        assertThat(creado.getDisponible()).isTrue();
        verify(platoValidator).validarNombreUnico(any(), isNull());
    }

    @Test
    void crearConservaLaDisponibilidadEnviada() {
        when(platoRepository.save(any(PlatoEntity.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        assertThat(platoService.crear(plato("Pulpo a la brasa", false)).getDisponible()).isFalse();
    }

    @Test
    void crearConNombreDuplicadoNoGuarda() {
        doThrow(new RecursoDuplicadoException("Duplicado")).when(platoValidator).validarNombreUnico(any(), isNull());

        assertThatThrownBy(() -> platoService.crear(plato("Papas trufadas", true)))
                .isInstanceOf(RecursoDuplicadoException.class);
        verify(platoRepository, never()).save(any());
    }

    @Test
    void listarYFiltrarPorCategoria() {
        PlatoEntity papas = entity(1L, true);
        when(platoRepository.findAllByOrderByIdAsc()).thenReturn(List.of(papas));
        when(platoRepository.findByCategoriaOrderByIdAsc(CategoriaPlato.POSTRES)).thenReturn(List.of());

        assertThat(platoService.listar()).extracting(Plato::getNombre).containsExactly("Papas trufadas");
        assertThat(platoService.listarPorCategoria(CategoriaPlato.POSTRES)).isEmpty();
    }

    @Test
    void obtenerPorIdInexistenteLanzaNoEncontrado() {
        when(platoRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> platoService.obtenerPorId(9L)).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void actualizarConservaDisponibilidadSiNoSeEnvia() {
        PlatoEntity existente = entity(1L, false);
        when(platoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(platoRepository.save(existente)).thenReturn(existente);

        Plato actualizado = platoService.actualizar(1L, plato(" Papas con trufa ", null));

        assertThat(actualizado.getNombre()).isEqualTo("Papas con trufa");
        assertThat(actualizado.getDisponible()).isFalse();
        verify(platoValidator).validarNombreUnico(" Papas con trufa ", 1L);
    }

    @Test
    void actualizarUsaLaDisponibilidadEnviada() {
        PlatoEntity existente = entity(1L, false);
        when(platoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(platoRepository.save(existente)).thenReturn(existente);

        assertThat(platoService.actualizar(1L, plato("Papas trufadas", true)).getDisponible()).isTrue();
    }

    @Test
    void cambiarDisponibilidadGuardaElNuevoValor() {
        PlatoEntity existente = entity(1L, true);
        when(platoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(platoRepository.save(existente)).thenReturn(existente);

        assertThat(platoService.cambiarDisponibilidad(1L, false).getDisponible()).isFalse();
    }

    @Test
    void eliminarExistenteYNoExistente() {
        PlatoEntity papas = entity(1L, true);
        when(platoRepository.findById(1L)).thenReturn(Optional.of(papas));
        when(platoRepository.findById(2L)).thenReturn(Optional.empty());

        platoService.eliminar(1L);

        verify(platoRepository).deleteById(1L);
        assertThatThrownBy(() -> platoService.eliminar(2L)).isInstanceOf(RecursoNoEncontradoException.class);
    }
}
