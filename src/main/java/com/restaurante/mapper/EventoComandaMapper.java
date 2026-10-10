package com.restaurante.mapper;

import com.restaurante.model.domain.EventoComanda;
import com.restaurante.model.dto.response.EventoComandaResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventoComandaMapper {

    EventoComandaResponseDTO toResponse(EventoComanda evento);

    List<EventoComandaResponseDTO> toResponseList(List<EventoComanda> eventos);
}
