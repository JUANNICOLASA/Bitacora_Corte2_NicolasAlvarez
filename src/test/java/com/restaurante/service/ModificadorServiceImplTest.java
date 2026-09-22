package com.restaurante.service;

import com.restaurante.exception.RecursoDuplicadoException;
import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.model.domain.Modificador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModificadorServiceImplTest {

    private ModificadorServiceImpl modificadorService;

    @BeforeEach
    void setUp() {
        modificadorService = new ModificadorServiceImpl();
    }

    private Modificador modificador(String nombre, Boolean disponible) {
        return Modificador.builder()
                .nombre(nombre)
                .graduacionAlcoholica(0.0)
                .precioExtra(2000.0)
                .disponible(disponible)
                .build();
    }

    @Test
    void crearAsignaIdYDisponiblePorDefecto() {
        Modificador creado = modificadorService.crear(modificador("Jarabe de agave", null));

        assertThat(creado.getId()).isEqualTo(1L);
        assertThat(creado.getDisponible()).isTrue();
        assertThat(modificadorService.listar()).containsExactly(creado);
    }

    @Test
    void crearConNombreDuplicadoLanzaConflicto() {
        modificadorService.crear(modificador("Jarabe de agave", true));

        Modificador duplicado = modificador("JARABE DE AGAVE", true);
        assertThatThrownBy(() -> modificadorService.crear(duplicado))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    void listarVacioDevuelveListaVacia() {
        assertThat(modificadorService.listar()).isEmpty();
    }

    @Test
    void obtenerPorIdInexistenteLanzaNoEncontrado() {
        assertThatThrownBy(() -> modificadorService.obtenerPorId(7L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void cambiarDisponibilidadMarcaAgotado() {
        Modificador creado = modificadorService.crear(modificador("Espuma de lavanda", true));

        Modificador resultado = modificadorService.cambiarDisponibilidad(creado.getId(), false);

        assertThat(resultado.getDisponible()).isFalse();
        assertThat(modificadorService.obtenerPorId(creado.getId()).estaDisponible()).isFalse();
    }
}
