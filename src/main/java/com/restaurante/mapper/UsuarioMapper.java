package com.restaurante.mapper;

import com.restaurante.model.domain.Usuario;
import com.restaurante.model.dto.request.RegistroRequestDTO;
import com.restaurante.model.dto.response.UsuarioResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rol", ignore = true)
    Usuario toDomain(RegistroRequestDTO dto);

    UsuarioResponseDTO toResponse(Usuario usuario);
}
