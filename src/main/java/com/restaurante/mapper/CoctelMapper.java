package com.restaurante.mapper;

import com.restaurante.model.domain.Coctel;
import com.restaurante.model.dto.request.CoctelRequestDTO;
import com.restaurante.model.dto.response.CoctelResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

// S07/S08 - Mapper IN (capa controller): RequestDTO -> dominio y dominio -> ResponseDTO.
// Son interfaces: MapStruct genera la implementacion al compilar. componentModel = "spring" lo vuelve un bean inyectable.
// Asi el controller nunca ve una entidad y el service nunca ve un DTO.
@Mapper(componentModel = "spring")
public interface CoctelMapper {

    // El id no viene en el request, lo asigna la BD
    @Mapping(target = "id", ignore = true)
    Coctel toDomain(CoctelRequestDTO dto);

    CoctelResponseDTO toResponse(Coctel coctel);

    List<CoctelResponseDTO> toResponseList(List<Coctel> cocteles);
}
