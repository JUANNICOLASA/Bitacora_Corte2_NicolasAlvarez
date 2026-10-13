package com.restaurante.mapper;

import com.restaurante.model.domain.Coctel;
import com.restaurante.persistence.entity.CoctelEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

// S09 - Mapper OUT (capa persistencia): dominio <-> entidad JPA. El dominio no sabe como se guarda.
@Mapper(componentModel = "spring")
public interface CoctelEntityMapper {

    @Mapping(target = "creadoEn", ignore = true)
    CoctelEntity toEntity(Coctel coctel);

    Coctel toDomain(CoctelEntity entity);

    // @MappingTarget actualiza la entidad que ya existe (PUT) en vez de crear una nueva
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    void actualizarEntity(Coctel coctel, @MappingTarget CoctelEntity entity);
}
