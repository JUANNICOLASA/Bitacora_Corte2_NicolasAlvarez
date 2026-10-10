package com.restaurante.mapper;

import com.restaurante.model.domain.EventoComanda;
import com.restaurante.persistence.document.EventoComandaDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventoComandaDocumentMapper {

    EventoComandaDocument toDocument(EventoComanda evento);

    EventoComanda toDomain(EventoComandaDocument document);
}
