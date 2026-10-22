package com.restaurante.model.dto.response;

import com.restaurante.model.domain.TipoProducto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Promedio y distribucion de las calificaciones de un producto")
public record ResumenCalificacionResponseDTO(
        TipoProducto tipo,
        Long idProducto,
        @Schema(example = "4.6") Double promedio,
        @Schema(example = "12") Long total,
        @Schema(description = "Cantidad de opiniones por numero de estrellas") Map<Integer, Long> distribucion
) {
}
