package com.restaurante.mapper;

import com.restaurante.model.domain.Coctel;
import com.restaurante.persistence.entity.CoctelEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CoctelEntityMapper {

    @Mapping(target = "creadoEn", ignore = true)
    CoctelEntity toEntity(Coctel coctel);

    Coctel toDomain(CoctelEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    void actualizarEntity(Coctel coctel, @MappingTarget CoctelEntity entity);
}
