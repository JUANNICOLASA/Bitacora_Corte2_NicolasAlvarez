package com.restaurante.mapper;

import com.restaurante.model.domain.Resena;
import com.restaurante.persistence.document.ResenaDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResenaDocumentMapper {

    ResenaDocument toDocument(Resena resena);

    Resena toDomain(ResenaDocument document);
}
