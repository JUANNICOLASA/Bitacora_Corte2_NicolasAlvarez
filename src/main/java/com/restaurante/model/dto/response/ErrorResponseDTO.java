package com.restaurante.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Respuesta uniforme de error")
public record ErrorResponseDTO(
        @Schema(example = "422") int status,
        @Schema(example = "BV-422") String codigo,
        @Schema(example = "El coctel Pina Colada esta AGOTADO EN BARRA") String mensaje,
        @Schema(example = "/api/v1/pedidos") String ruta,
        LocalDateTime timestamp,
        List<String> detalles
) {
}
