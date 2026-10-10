package com.restaurante.mapper;

import com.restaurante.model.domain.Usuario;
import com.restaurante.persistence.entity.UsuarioEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioEntityMapper {

    UsuarioEntity toEntity(Usuario usuario);

    Usuario toDomain(UsuarioEntity entity);
}
