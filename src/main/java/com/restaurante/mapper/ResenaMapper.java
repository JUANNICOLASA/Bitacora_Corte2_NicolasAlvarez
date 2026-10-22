package com.restaurante.mapper;

import com.restaurante.model.domain.Resena;
import com.restaurante.model.domain.ResumenCalificacion;
import com.restaurante.model.dto.request.ResenaRequestDTO;
import com.restaurante.model.dto.response.ResenaResponseDTO;
import com.restaurante.model.dto.response.ResumenCalificacionResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ResenaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "autor", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "fecha", ignore = true)
    Resena toDomain(ResenaRequestDTO dto);

    ResenaResponseDTO toResponse(Resena resena);

    List<ResenaResponseDTO> toResponseList(List<Resena> resenas);

    ResumenCalificacionResponseDTO toResumenResponse(ResumenCalificacion resumen);

    List<ResumenCalificacionResponseDTO> toResumenResponseList(List<ResumenCalificacion> resumenes);
}
