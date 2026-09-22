package com.restaurante.mapper;

import com.restaurante.model.domain.Modificador;
import com.restaurante.model.dto.request.ModificadorRequestDTO;
import com.restaurante.model.dto.response.ModificadorResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Transforma modificadores entre DTOs y dominio.
 */
@Mapper(componentModel = "spring")
public interface ModificadorMapper {

    @Mapping(target = "id", ignore = true)
    Modificador toDomain(ModificadorRequestDTO dto);

    ModificadorResponseDTO toResponse(Modificador modificador);

    List<ModificadorResponseDTO> toResponseList(List<Modificador> modificadores);
}
