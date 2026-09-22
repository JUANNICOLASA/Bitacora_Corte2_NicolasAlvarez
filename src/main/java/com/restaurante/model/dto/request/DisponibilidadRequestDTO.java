package com.restaurante.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Cambio de disponibilidad en barra")
public record DisponibilidadRequestDTO(

        @Schema(description = "true = disponible, false = agotado en barra", example = "false")
        @NotNull(message = "El campo disponible es obligatorio")
        Boolean disponible
) {
}
