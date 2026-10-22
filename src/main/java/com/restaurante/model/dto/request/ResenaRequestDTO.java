package com.restaurante.model.dto.request;

import com.restaurante.model.domain.TipoProducto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Calificacion de un coctel o de un plato")
public record ResenaRequestDTO(

        @Schema(description = "COCTEL o PLATO", example = "COCTEL")
        @NotNull(message = "El tipo de producto es obligatorio")
        TipoProducto tipo,

        @Schema(description = "Id del coctel o del plato", example = "1")
        @NotNull(message = "El id del producto es obligatorio")
        Long idProducto,

        @Schema(description = "Estrellas de 1 a 5", example = "5")
        @NotNull(message = "Las estrellas son obligatorias")
        @Min(value = 1, message = "La calificacion minima es 1 estrella")
        @Max(value = 5, message = "La calificacion maxima es 5 estrellas")
        Integer estrellas,

        @Schema(description = "Comentario opcional", example = "Fresco y bien balanceado")
        @Size(max = 280, message = "El comentario no puede superar 280 caracteres")
        String comentario
) {
}
