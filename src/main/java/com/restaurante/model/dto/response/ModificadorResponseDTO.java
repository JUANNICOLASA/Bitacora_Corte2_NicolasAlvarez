package com.restaurante.model.dto.response;

public record ModificadorResponseDTO(
        Long id,
        String nombre,
        Double graduacionAlcoholica,
        Double precioExtra,
        Boolean disponible
) {
}
