package com.restaurante.repository;

import com.restaurante.persistence.document.EventoComandaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// S09 - MongoRepository<Documento, TipoDelId>: mismo patron que JPA pero para Mongo. 1 repositorio por documento.
@Repository
public interface EventoComandaRepository extends MongoRepository<EventoComandaDocument, String> {

    List<EventoComandaDocument> findAllByOrderByFechaDesc();

    List<EventoComandaDocument> findByIdPedidoOrderByFechaAsc(Long idPedido);
}
