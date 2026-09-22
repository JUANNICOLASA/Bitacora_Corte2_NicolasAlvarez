package com.restaurante.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Cambio de licor de un coctel (permitido una sola vez)")
public record CambioDestiladoRequestDTO(

        @Schema(description = "Nueva marca o tipo exacto de destilado", example = "Hendrick's")
        @NotBlank(message = "El destilado es obligatorio")
        @Size(max = 60, message = "El destilado no puede superar 60 caracteres")
        String destilado
) {
}
