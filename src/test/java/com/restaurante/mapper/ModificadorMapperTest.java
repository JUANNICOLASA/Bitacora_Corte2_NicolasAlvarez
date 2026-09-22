package com.restaurante.mapper;

import com.restaurante.model.domain.Modificador;
import com.restaurante.model.dto.request.ModificadorRequestDTO;
import com.restaurante.model.dto.response.ModificadorResponseDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ModificadorMapperTest {

    private final ModificadorMapper mapper = Mappers.getMapper(ModificadorMapper.class);

    @Test
    void requestADominio() {
        Modificador modificador = mapper.toDomain(new ModificadorRequestDTO("Shot extra", 40.0, 9000.0, null));

        assertThat(modificador.getId()).isNull();
        assertThat(modificador.getNombre()).isEqualTo("Shot extra");
        assertThat(modificador.getGraduacionAlcoholica()).isEqualTo(40.0);
        assertThat(modificador.getPrecioExtra()).isEqualTo(9000.0);
        assertThat(modificador.getDisponible()).isNull();
    }

    @Test
    void dominioAResponse() {
        Modificador modificador = Modificador.builder().id(2L).nombre("Jarabe de agave")
                .graduacionAlcoholica(0.0).precioExtra(2000.0).disponible(true).build();

        ModificadorResponseDTO response = mapper.toResponse(modificador);

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.nombre()).isEqualTo("Jarabe de agave");
        assertThat(response.disponible()).isTrue();
    }

    @Test
    void listaYNulos() {
        assertThat(mapper.toResponseList(List.of(Modificador.builder().id(1L).build()))).hasSize(1);
        assertThat(mapper.toResponse(null)).isNull();
        assertThat(mapper.toDomain(null)).isNull();
        assertThat(mapper.toResponseList(null)).isNull();
    }
}
