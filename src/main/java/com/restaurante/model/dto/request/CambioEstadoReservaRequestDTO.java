package com.restaurante.model.dto.request;

import com.restaurante.model.domain.EstadoReserva;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Nuevo estado de la reserva")
public record CambioEstadoReservaRequestDTO(

        @Schema(description = "Estado destino", example = "CUMPLIDA")
        @NotNull(message = "El estado es obligatorio")
        EstadoReserva estado
) {
}
