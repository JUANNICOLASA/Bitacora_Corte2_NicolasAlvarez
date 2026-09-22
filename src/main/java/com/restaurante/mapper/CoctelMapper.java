package com.restaurante.mapper;

import com.restaurante.model.domain.Coctel;
import com.restaurante.model.dto.request.CoctelRequestDTO;
import com.restaurante.model.dto.response.CoctelResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Transforma cocteles entre DTOs y dominio.
 */
@Mapper(componentModel = "spring")
public interface CoctelMapper {

    @Mapping(target = "id", ignore = true)
    Coctel toDomain(CoctelRequestDTO dto);

    CoctelResponseDTO toResponse(Coctel coctel);

    List<CoctelResponseDTO> toResponseList(List<Coctel> cocteles);
}
