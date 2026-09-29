package com.restaurante.mapper;

import com.restaurante.model.domain.Modificador;
import com.restaurante.persistence.entity.ModificadorEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ModificadorEntityMapper {

    ModificadorEntity toEntity(Modificador modificador);

    Modificador toDomain(ModificadorEntity entity);
}
