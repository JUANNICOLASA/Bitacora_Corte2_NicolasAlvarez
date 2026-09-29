package com.restaurante.service;

import com.restaurante.exception.RecursoDuplicadoException;
import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.ModificadorEntityMapper;
import com.restaurante.model.domain.Modificador;
import com.restaurante.persistence.entity.ModificadorEntity;
import com.restaurante.repository.ModificadorRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModificadorServiceImplTest {

    @Mock
    private ModificadorRepository modificadorRepository;

    @Spy
    private ModificadorEntityMapper modificadorEntityMapper = Mappers.getMapper(ModificadorEntityMapper.class);

    @InjectMocks
    private ModificadorServiceImpl modificadorService;

    private Modificador modificador(String nombre, Boolean disponible) {
        return Modificador.builder()
                .nombre(nombre)
                .graduacionAlcoholica(0.0)
                .precioExtra(2000.0)
                .disponible(disponible)
                .build();
    }

    private ModificadorEntity entity(Long id, String nombre, boolean disponible) {
        ModificadorEntity entity = new ModificadorEntity();
        entity.setId(id);
        entity.setNombre(nombre);
        entity.setGraduacionAlcoholica(0.0);
        entity.setPrecioExtra(2000.0);
        entity.setDisponible(disponible);
        return entity;
    }

    @Test
    void crearGuardaYAsignaDisponiblePorDefecto() {
        when(modificadorRepository.existsByNombreIgnoreCase("Jarabe de agave")).thenReturn(false);
        when(modificadorRepository.save(any(ModificadorEntity.class))).thenAnswer(invocacion -> {
            ModificadorEntity guardado = invocacion.getArgument(0);
            guardado.setId(1L);
            return guardado;
        });

        Modificador creado = modificadorService.crear(modificador(" Jarabe de agave ", null));

        assertThat(creado.getId()).isEqualTo(1L);
        assertThat(creado.getNombre()).isEqualTo("Jarabe de agave");
        assertThat(creado.getDisponible()).isTrue();
    }

    @Test
    void crearConNombreDuplicadoLanzaConflicto() {
        when(modificadorRepository.existsByNombreIgnoreCase("JARABE DE AGAVE")).thenReturn(true);

        Modificador duplicado = modificador("JARABE DE AGAVE", true);
        assertThatThrownBy(() -> modificadorService.crear(duplicado))
                .isInstanceOf(RecursoDuplicadoException.class);
        verify(modificadorRepository, never()).save(any());
    }

    @Test
    void listarVacioDevuelveListaVacia() {
        when(modificadorRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

        assertThat(modificadorService.listar()).isEmpty();
    }

    @Test
    void listarConvierteEntidades() {
        when(modificadorRepository.findAllByOrderByIdAsc()).thenReturn(List.of(entity(1L, "Twist de naranja", true)));

        assertThat(modificadorService.listar()).extracting(Modificador::getNombre).containsExactly("Twist de naranja");
    }

    @Test
    void obtenerPorIdInexistenteLanzaNoEncontrado() {
        when(modificadorRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> modificadorService.obtenerPorId(7L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void cambiarDisponibilidadMarcaAgotado() {
        ModificadorEntity existente = entity(3L, "Espuma de lavanda", true);
        when(modificadorRepository.findById(3L)).thenReturn(Optional.of(existente));
        when(modificadorRepository.save(existente)).thenReturn(existente);

        Modificador resultado = modificadorService.cambiarDisponibilidad(3L, false);

        assertThat(resultado.getDisponible()).isFalse();
        assertThat(existente.getDisponible()).isFalse();
    }
}
