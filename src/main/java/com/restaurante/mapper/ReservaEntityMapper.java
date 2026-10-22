package com.restaurante.mapper;

import com.restaurante.model.domain.Reserva;
import com.restaurante.persistence.entity.ReservaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservaEntityMapper {

    ReservaEntity toEntity(Reserva reserva);

    Reserva toDomain(ReservaEntity entity);
}
