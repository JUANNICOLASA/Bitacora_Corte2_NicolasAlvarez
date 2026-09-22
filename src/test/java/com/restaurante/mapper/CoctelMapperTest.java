package com.restaurante.mapper;

import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.model.dto.request.CoctelRequestDTO;
import com.restaurante.model.dto.response.CoctelResponseDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoctelMapperTest {

    private final CoctelMapper mapper = Mappers.getMapper(CoctelMapper.class);

    @Test
    void requestADominioSinId() {
        CoctelRequestDTO request = new CoctelRequestDTO("Negroni", "Gin, vermut y Campari", 38000.0,
                CategoriaCoctel.CLASICO, TipoBebida.ALCOHOLICA, "Gin", true);

        Coctel coctel = mapper.toDomain(request);

        assertThat(coctel.getId()).isNull();
        assertThat(coctel.getNombre()).isEqualTo("Negroni");
        assertThat(coctel.getPrecio()).isEqualTo(38000.0);
        assertThat(coctel.getCategoria()).isEqualTo(CategoriaCoctel.CLASICO);
        assertThat(coctel.getTipo()).isEqualTo(TipoBebida.ALCOHOLICA);
        assertThat(coctel.getDestiladoBase()).isEqualTo("Gin");
        assertThat(coctel.getDisponible()).isTrue();
    }

    @Test
    void dominioAResponse() {
        Coctel coctel = Coctel.builder().id(3L).nombre("Virgin Mojito").precio(22000.0)
                .categoria(CategoriaCoctel.SIN_ALCOHOL).tipo(TipoBebida.MOCKTAIL).disponible(false).build();

        CoctelResponseDTO response = mapper.toResponse(coctel);

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.nombre()).isEqualTo("Virgin Mojito");
        assertThat(response.tipo()).isEqualTo(TipoBebida.MOCKTAIL);
        assertThat(response.disponible()).isFalse();
        assertThat(response.destiladoBase()).isNull();
    }

    @Test
    void listaYNulos() {
        assertThat(mapper.toResponseList(List.of(Coctel.builder().id(1L).build()))).hasSize(1);
        assertThat(mapper.toResponse(null)).isNull();
        assertThat(mapper.toDomain(null)).isNull();
        assertThat(mapper.toResponseList(null)).isNull();
    }
}
