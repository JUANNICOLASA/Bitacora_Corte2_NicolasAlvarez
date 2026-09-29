package com.restaurante.mapper;

import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.persistence.entity.CoctelEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CoctelEntityMapperTest {

    private final CoctelEntityMapper mapper = Mappers.getMapper(CoctelEntityMapper.class);

    private Coctel negroni() {
        return Coctel.builder().id(2L).nombre("Negroni").descripcion("Gin, vermut y Campari").precio(38000.0)
                .categoria(CategoriaCoctel.CLASICO).tipo(TipoBebida.ALCOHOLICA).destiladoBase("Gin")
                .disponible(true).build();
    }

    @Test
    void dominioAEntidad() {
        CoctelEntity entity = mapper.toEntity(negroni());

        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getNombre()).isEqualTo("Negroni");
        assertThat(entity.getCategoria()).isEqualTo(CategoriaCoctel.CLASICO);
        assertThat(entity.getDestiladoBase()).isEqualTo("Gin");
        assertThat(entity.getCreadoEn()).isNull();
    }

    @Test
    void entidadADominio() {
        CoctelEntity entity = mapper.toEntity(negroni());
        entity.setCreadoEn(LocalDateTime.now());

        Coctel coctel = mapper.toDomain(entity);

        assertThat(coctel.getId()).isEqualTo(2L);
        assertThat(coctel.getTipo()).isEqualTo(TipoBebida.ALCOHOLICA);
        assertThat(coctel.getDisponible()).isTrue();
    }

    @Test
    void actualizarEntidadConservaIdYFechaDeCreacion() {
        CoctelEntity entity = mapper.toEntity(negroni());
        LocalDateTime creado = LocalDateTime.of(2026, 9, 1, 20, 0);
        entity.setCreadoEn(creado);
        Coctel cambios = negroni();
        cambios.setId(99L);
        cambios.setNombre("Negroni Sbagliato");
        cambios.setPrecio(40000.0);

        mapper.actualizarEntity(cambios, entity);

        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getCreadoEn()).isEqualTo(creado);
        assertThat(entity.getNombre()).isEqualTo("Negroni Sbagliato");
        assertThat(entity.getPrecio()).isEqualTo(40000.0);
    }

    @Test
    void nulos() {
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toDomain(null)).isNull();
    }
}
