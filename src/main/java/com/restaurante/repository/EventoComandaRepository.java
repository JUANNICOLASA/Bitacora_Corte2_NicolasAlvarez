package com.restaurante.repository;

import com.restaurante.persistence.document.EventoComandaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoComandaRepository extends MongoRepository<EventoComandaDocument, String> {

    List<EventoComandaDocument> findAllByOrderByFechaDesc();

    List<EventoComandaDocument> findByIdPedidoOrderByFechaAsc(Long idPedido);
}
