package com.restaurante.mapper;

import com.restaurante.client.dto.BebidaExternaDTO;
import com.restaurante.model.domain.CategoriaPlato;
import com.restaurante.model.domain.Plato;
import com.restaurante.model.domain.RecetaExterna;
import com.restaurante.model.domain.Resena;
import com.restaurante.model.domain.Reserva;
import com.restaurante.model.domain.ResumenCalificacion;
import com.restaurante.model.domain.TipoProducto;
import com.restaurante.model.dto.request.PlatoRequestDTO;
import com.restaurante.model.dto.request.ResenaRequestDTO;
import com.restaurante.model.dto.request.ReservaRequestDTO;
import com.restaurante.persistence.document.ResenaDocument;
import com.restaurante.persistence.entity.PlatoEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class NuevosMappersTest {

    @Test
    void platoDeRequestADominioYEntidad() {
        Plato plato = Mappers.getMapper(PlatoMapper.class).toDomain(new PlatoRequestDTO("Papas trufadas", null,
                22000.0, CategoriaPlato.PARA_COMPARTIR, "Para compartir", null));
        PlatoEntityMapper entityMapper = Mappers.getMapper(PlatoEntityMapper.class);
        PlatoEntity entity = entityMapper.toEntity(plato);
        entity.setId(3L);
        entityMapper.actualizarEntity(Plato.builder().id(99L).nombre("Papas").precio(1.0).build(), entity);

        assertThat(plato.getId()).isNull();
        assertThat(entity.getId()).isEqualTo(3L);
        assertThat(entity.getNombre()).isEqualTo("Papas");
        assertThat(entityMapper.toDomain(entity).getPrecio()).isEqualTo(1.0);
    }

    @Test
    void resenaIgnoraAutorYUsuarioDelRequest() {
        Resena resena = Mappers.getMapper(ResenaMapper.class)
                .toDomain(new ResenaRequestDTO(TipoProducto.PLATO, 2L, 4, "Rico"));
        ResenaDocument documento = Mappers.getMapper(ResenaDocumentMapper.class).toDocument(resena);

        assertThat(resena.getAutor()).isNull();
        assertThat(resena.getUsuario()).isNull();
        assertThat(documento.getIdProducto()).isEqualTo(2L);
        assertThat(Mappers.getMapper(ResenaDocumentMapper.class).toDomain(documento).getEstrellas()).isEqualTo(4);
    }

    @Test
    void reservaDeRequestADominio() {
        Reserva reserva = Mappers.getMapper(ReservaMapper.class).toDomain(new ReservaRequestDTO("Laura Gomez",
                "3005550186", "laura@correo.com", LocalDate.of(2026, 12, 4), LocalTime.of(20, 0), 2, null, null));

        assertThat(reserva.getEstado()).isNull();
        assertThat(reserva.getCodigo()).isNull();
        assertThat(Mappers.getMapper(ReservaEntityMapper.class).toEntity(reserva).getPersonas()).isEqualTo(2);
    }

    @Test
    void recetaExternaNulaYCompleta() {
        RecetaExternaMapper mapper = Mappers.getMapper(RecetaExternaMapper.class);
        BebidaExternaDTO bebida = new BebidaExternaDTO();
        bebida.setNombre("Negroni");
        bebida.setInstruccionesEs("Remover con hielo.");
        bebida.agregarCampo("strIngredient1", "Gin");
        bebida.agregarCampo("strMeasure1", null);
        bebida.agregarCampo("strIngredient2", " ");

        RecetaExterna receta = mapper.toDomain(bebida);

        assertThat(mapper.toDomain(null)).isNull();
        assertThat(receta.getIngredientes()).containsExactly("Gin");
        assertThat(mapper.toResponse(receta).instrucciones()).isEqualTo("Remover con hielo.");
    }

    @Test
    void entradasNulasDevuelvenNulo() {
        PlatoMapper platoMapper = Mappers.getMapper(PlatoMapper.class);
        PlatoEntityMapper platoEntityMapper = Mappers.getMapper(PlatoEntityMapper.class);
        ResenaMapper resenaMapper = Mappers.getMapper(ResenaMapper.class);
        ResenaDocumentMapper resenaDocumentMapper = Mappers.getMapper(ResenaDocumentMapper.class);
        ReservaMapper reservaMapper = Mappers.getMapper(ReservaMapper.class);
        ReservaEntityMapper reservaEntityMapper = Mappers.getMapper(ReservaEntityMapper.class);
        PlatoEntity entity = new PlatoEntity();
        platoEntityMapper.actualizarEntity(null, entity);

        assertThat(platoMapper.toDomain(null)).isNull();
        assertThat(platoMapper.toResponse(null)).isNull();
        assertThat(platoMapper.toResponseList(null)).isNull();
        assertThat(platoEntityMapper.toEntity(null)).isNull();
        assertThat(platoEntityMapper.toDomain(null)).isNull();
        assertThat(resenaMapper.toDomain(null)).isNull();
        assertThat(resenaMapper.toResponse(null)).isNull();
        assertThat(resenaMapper.toResponseList(null)).isNull();
        assertThat(resenaMapper.toResumenResponse(null)).isNull();
        assertThat(resenaMapper.toResumenResponseList(null)).isNull();
        assertThat(resenaDocumentMapper.toDocument(null)).isNull();
        assertThat(resenaDocumentMapper.toDomain(null)).isNull();
        assertThat(reservaMapper.toDomain(null)).isNull();
        assertThat(reservaMapper.toResponse(null)).isNull();
        assertThat(reservaMapper.toResponseList(null)).isNull();
        assertThat(reservaEntityMapper.toEntity(null)).isNull();
        assertThat(reservaEntityMapper.toDomain(null)).isNull();
        assertThat(Mappers.getMapper(RecetaExternaMapper.class).toResponse(null)).isNull();
        assertThat(resenaMapper.toResumenResponse(ResumenCalificacion.builder().build())
                .distribucion()).isNull();
    }
}
