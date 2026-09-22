package com.restaurante.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear un modificador (adicion de un coctel)")
public record ModificadorRequestDTO(

        @Schema(description = "Nombre del modificador", example = "Shot extra de gin")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "El nombre no puede superar 60 caracteres")
        String nombre,

        @Schema(description = "Graduacion alcoholica en %. 0 si no tiene alcohol", example = "40")
        @NotNull(message = "La graduacion alcoholica es obligatoria")
        @PositiveOrZero(message = "La graduacion no puede ser negativa")
        @DecimalMax(value = "100.0", message = "La graduacion no puede superar 100")
        Double graduacionAlcoholica,

        @Schema(description = "Precio adicional en pesos colombianos", example = "9000")
        @NotNull(message = "El precio extra es obligatorio")
        @PositiveOrZero(message = "El precio extra no puede ser negativo")
        Double precioExtra,

        @Schema(description = "Si esta disponible en barra. Por defecto true", example = "true")
        Boolean disponible
) {
}
