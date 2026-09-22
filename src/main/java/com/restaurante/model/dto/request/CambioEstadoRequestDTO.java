package com.restaurante.model.dto.request;

import com.restaurante.model.domain.EstadoPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Nuevo estado de la comanda en el KDS")
public record CambioEstadoRequestDTO(

        @Schema(description = "Estado destino", example = "EN_PREPARACION")
        @NotNull(message = "El estado es obligatorio")
        EstadoPedido estado
) {
}
