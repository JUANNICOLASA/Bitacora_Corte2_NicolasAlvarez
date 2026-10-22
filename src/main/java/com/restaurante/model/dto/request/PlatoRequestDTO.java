package com.restaurante.model.dto.request;

import com.restaurante.model.domain.CategoriaPlato;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear o actualizar un plato de la cocina")
public record PlatoRequestDTO(

        @Schema(description = "Nombre del plato", example = "Papas trufadas")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "El nombre no puede superar 60 caracteres")
        String nombre,

        @Schema(description = "Descripcion para la carta", example = "Papas rusticas, aceite de trufa y parmesano")
        @Size(max = 250, message = "La descripcion no puede superar 250 caracteres")
        String descripcion,

        @Schema(description = "Precio en pesos colombianos", example = "22000")
        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor a 0")
        Double precio,

        @Schema(description = "Categoria de la cocina", example = "PARA_COMPARTIR")
        @NotNull(message = "La categoria es obligatoria")
        CategoriaPlato categoria,

        @Schema(description = "Porcion o tamano del plato", example = "Para compartir")
        @Size(max = 60, message = "La porcion no puede superar 60 caracteres")
        String porcion,

        @Schema(description = "Si el plato esta disponible. Por defecto true", example = "true")
        Boolean disponible
) {
}
