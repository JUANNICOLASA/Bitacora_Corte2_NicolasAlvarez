package com.restaurante.model.dto.response;

import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.TipoBebida;

// ResponseDTO: controla exactamente que informacion sale de la API
public record CoctelResponseDTO(
        Long id,
        String nombre,
        String descripcion,
        Double precio,
        CategoriaCoctel categoria,
        TipoBebida tipo,
        String destiladoBase,
        Boolean disponible
) {
}
