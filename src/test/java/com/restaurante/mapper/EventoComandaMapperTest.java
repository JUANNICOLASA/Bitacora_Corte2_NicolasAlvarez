package com.restaurante.mapper;

import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.EventoComanda;
import com.restaurante.model.domain.TipoEvento;
import com.restaurante.model.dto.response.EventoComandaResponseDTO;
import com.restaurante.persistence.document.EventoComandaDocument;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventoComandaMapperTest {

    private final EventoComandaMapper mapper = Mappers.getMapper(EventoComandaMapper.class);
    private final EventoComandaDocumentMapper documentMapper = Mappers.getMapper(EventoComandaDocumentMapper.class);

    private final EventoComanda evento = EventoComanda.builder().id("abc123").idPedido(4L)
            .tipo(TipoEvento.CAMBIO_LICOR).estado(EstadoPedido.RECIBIDO)
            .descripcion("Licor del item 1 cambiado de Tanqueray a Bombay")
            .usuario("mesero@bluevelvet.com").fecha(LocalDateTime.of(2026, 10, 10, 20, 30)).build();

    @Test
    void dominioYDocumento() {
        EventoComandaDocument documento = documentMapper.toDocument(evento);

        assertThat(documento.getIdPedido()).isEqualTo(4L);
        assertThat(documento.getTipo()).isEqualTo(TipoEvento.CAMBIO_LICOR);
        assertThat(documentMapper.toDomain(documento)).isEqualTo(evento);
        assertThat(documentMapper.toDocument(null)).isNull();
        assertThat(documentMapper.toDomain(null)).isNull();
    }

    @Test
    void dominioARespuesta() {
        EventoComandaResponseDTO respuesta = mapper.toResponse(evento);

        assertThat(respuesta.usuario()).isEqualTo("mesero@bluevelvet.com");
        assertThat(mapper.toResponseList(List.of(evento))).hasSize(1);
        assertThat(mapper.toResponse(null)).isNull();
        assertThat(mapper.toResponseList(null)).isNull();
    }
}
