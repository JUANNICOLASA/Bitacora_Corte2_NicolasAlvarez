package com.restaurante.mapper;

import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.Modificador;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.model.dto.response.MenuItemResponseDTO;
import com.restaurante.model.dto.response.ModificadorMenuResponseDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MenuMapperTest {

    private final MenuMapper mapper = Mappers.getMapper(MenuMapper.class);

    private final Coctel mocktail = Coctel.builder().id(6L).nombre("Virgin Mojito")
            .categoria(CategoriaCoctel.SIN_ALCOHOL).tipo(TipoBebida.MOCKTAIL).disponible(true).build();

    @Test
    void coctelAgotadoLlevaEtiquetaCandadoYNoEsSeleccionable() {
        Coctel agotado = Coctel.builder().id(5L).nombre("Pina Colada")
                .tipo(TipoBebida.ALCOHOLICA).disponible(false).build();

        MenuItemResponseDTO item = mapper.toMenuItem(agotado);

        assertThat(item.id()).isEqualTo(5L);
        assertThat(item.disponible()).isFalse();
        assertThat(item.etiqueta()).isEqualTo("AGOTADO EN BARRA");
        assertThat(item.icono()).isEqualTo("candado");
        assertThat(item.seleccionable()).isFalse();
    }

    @Test
    void coctelDisponibleEsSeleccionable() {
        List<MenuItemResponseDTO> items = mapper.toMenuItems(List.of(mocktail));

        assertThat(items).hasSize(1);
        assertThat(items.get(0).etiqueta()).isEqualTo("DISPONIBLE");
        assertThat(items.get(0).seleccionable()).isTrue();
    }

    @Test
    void modificadorConAlcoholSeBloqueaEnMocktail() {
        Modificador shot = Modificador.builder().id(1L).nombre("Shot extra")
                .graduacionAlcoholica(40.0).precioExtra(9000.0).disponible(true).build();
        Modificador agave = Modificador.builder().id(3L).nombre("Jarabe de agave")
                .graduacionAlcoholica(0.0).precioExtra(2000.0).disponible(true).build();

        List<ModificadorMenuResponseDTO> resultado = mapper.toModificadoresMenu(List.of(shot, agave), mocktail);

        assertThat(resultado.get(0).habilitado()).isFalse();
        assertThat(resultado.get(0).etiqueta()).isEqualTo("NO DISPONIBLE PARA MOCKTAIL");
        assertThat(resultado.get(0).icono()).isEqualTo("candado");
        assertThat(resultado.get(1).habilitado()).isTrue();
        assertThat(resultado.get(1).etiqueta()).isEqualTo("DISPONIBLE");
    }

    @Test
    void nulos() {
        assertThat(mapper.toMenuItem(null)).isNull();
        assertThat(mapper.toMenuItems(null)).isNull();
    }
}
