package com.restaurante.model.dto.response;

import com.restaurante.model.domain.CategoriaPlato;

public record PlatoResponseDTO(
        Long id,
        String nombre,
        String descripcion,
        Double precio,
        CategoriaPlato categoria,
        String porcion,
        Boolean disponible
) {
}
