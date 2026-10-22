package com.restaurante.mapper;

import com.restaurante.model.domain.Plato;
import com.restaurante.persistence.entity.PlatoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PlatoEntityMapper {

    PlatoEntity toEntity(Plato plato);

    Plato toDomain(PlatoEntity entity);

    @Mapping(target = "id", ignore = true)
    void actualizarEntity(Plato plato, @MappingTarget PlatoEntity entity);
}
