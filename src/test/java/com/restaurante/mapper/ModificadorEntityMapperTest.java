package com.restaurante.mapper;

import com.restaurante.model.domain.Modificador;
import com.restaurante.persistence.entity.ModificadorEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class ModificadorEntityMapperTest {

    private final ModificadorEntityMapper mapper = Mappers.getMapper(ModificadorEntityMapper.class);

    @Test
    void idaYVuelta() {
        Modificador shot = Modificador.builder().id(1L).nombre("Shot extra de destilado")
                .graduacionAlcoholica(40.0).precioExtra(9000.0).disponible(true).build();

        ModificadorEntity entity = mapper.toEntity(shot);
        Modificador resultado = mapper.toDomain(entity);

        assertThat(entity.getGraduacionAlcoholica()).isEqualTo(40.0);
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Shot extra de destilado");
        assertThat(resultado.getPrecioExtra()).isEqualTo(9000.0);
        assertThat(resultado.esAlcoholico()).isTrue();
    }

    @Test
    void nulos() {
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toDomain(null)).isNull();
    }
}
