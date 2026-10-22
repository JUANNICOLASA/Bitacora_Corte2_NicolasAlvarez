package com.restaurante.model.dto.response;

import com.restaurante.model.domain.TipoProducto;

import java.time.LocalDateTime;

public record ResenaResponseDTO(
        String id,
        TipoProducto tipo,
        Long idProducto,
        String autor,
        Integer estrellas,
        String comentario,
        LocalDateTime fecha
) {
}
