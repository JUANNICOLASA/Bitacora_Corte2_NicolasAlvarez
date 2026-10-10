package com.restaurante.service;

import com.restaurante.mapper.EventoComandaDocumentMapper;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.EventoComanda;
import com.restaurante.model.domain.TipoEvento;
import com.restaurante.persistence.document.EventoComandaDocument;
import com.restaurante.repository.EventoComandaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventoComandaServiceImpl implements EventoComandaService {

    private final EventoComandaRepository eventoComandaRepository;
    private final EventoComandaDocumentMapper eventoComandaDocumentMapper;

    @Override
    public EventoComanda registrar(Long idPedido, TipoEvento tipo, EstadoPedido estado, String descripcion,
                                   String usuario) {
        EventoComanda evento = EventoComanda.builder()
                .idPedido(idPedido)
                .tipo(tipo)
                .estado(estado)
                .descripcion(descripcion)
                .usuario(usuario)
                .fecha(LocalDateTime.now())
                .build();
        EventoComandaDocument guardado = eventoComandaRepository.save(eventoComandaDocumentMapper.toDocument(evento));
        log.info("Evento {} registrado para la comanda id={}", tipo, idPedido);
        return eventoComandaDocumentMapper.toDomain(guardado);
    }

    @Override
    public List<EventoComanda> listar(Long idPedido) {
        List<EventoComandaDocument> documentos = idPedido == null
                ? eventoComandaRepository.findAllByOrderByFechaDesc()
                : eventoComandaRepository.findByIdPedidoOrderByFechaAsc(idPedido);
        return documentos.stream()
                .map(eventoComandaDocumentMapper::toDomain)
                .toList();
    }
}
