package com.restaurante.service;

import com.restaurante.mapper.EventoComandaDocumentMapper;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.EventoComanda;
import com.restaurante.model.domain.TipoEvento;
import com.restaurante.persistence.document.EventoComandaDocument;
import com.restaurante.repository.EventoComandaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventoComandaServiceImplTest {

    @Mock
    private EventoComandaRepository eventoComandaRepository;

    @Spy
    private EventoComandaDocumentMapper eventoComandaDocumentMapper =
            Mappers.getMapper(EventoComandaDocumentMapper.class);

    @InjectMocks
    private EventoComandaServiceImpl eventoComandaService;

    private EventoComandaDocument documento(Long idPedido, TipoEvento tipo) {
        EventoComandaDocument documento = new EventoComandaDocument();
        documento.setId("abc123");
        documento.setIdPedido(idPedido);
        documento.setTipo(tipo);
        return documento;
    }

    @Test
    void registrarGuardaElEventoConFechaYUsuario() {
        when(eventoComandaRepository.save(any(EventoComandaDocument.class))).thenAnswer(invocacion -> {
            EventoComandaDocument documento = invocacion.getArgument(0);
            documento.setId("abc123");
            return documento;
        });

        EventoComanda evento = eventoComandaService.registrar(1L, TipoEvento.CREADA, EstadoPedido.RECIBIDO,
                "Comanda creada", "mesero@bluevelvet.com");

        assertThat(evento.getId()).isEqualTo("abc123");
        assertThat(evento.getIdPedido()).isEqualTo(1L);
        assertThat(evento.getTipo()).isEqualTo(TipoEvento.CREADA);
        assertThat(evento.getEstado()).isEqualTo(EstadoPedido.RECIBIDO);
        assertThat(evento.getUsuario()).isEqualTo("mesero@bluevelvet.com");
        assertThat(evento.getFecha()).isNotNull();
    }

    @Test
    void listarSinFiltroDevuelveTodos() {
        when(eventoComandaRepository.findAllByOrderByFechaDesc())
                .thenReturn(List.of(documento(1L, TipoEvento.CREADA), documento(2L, TipoEvento.CANCELADA)));

        assertThat(eventoComandaService.listar(null)).hasSize(2);
    }

    @Test
    void listarPorComanda() {
        when(eventoComandaRepository.findByIdPedidoOrderByFechaAsc(1L))
                .thenReturn(List.of(documento(1L, TipoEvento.CREADA)));

        assertThat(eventoComandaService.listar(1L)).extracting(EventoComanda::getTipo)
                .containsExactly(TipoEvento.CREADA);
    }
}
