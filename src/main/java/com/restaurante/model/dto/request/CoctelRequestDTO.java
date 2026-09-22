package com.restaurante.model.dto.request;

import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.TipoBebida;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear o actualizar un coctel de la carta")
public record CoctelRequestDTO(

        @Schema(description = "Nombre del coctel", example = "Negroni")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "El nombre no puede superar 60 caracteres")
        String nombre,

        @Schema(description = "Descripcion para la carta", example = "Gin, vermut rojo y Campari")
        @Size(max = 250, message = "La descripcion no puede superar 250 caracteres")
        String descripcion,

        @Schema(description = "Precio en pesos colombianos", example = "38000")
        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor a 0")
        Double precio,

        @Schema(description = "Categoria de la carta", example = "CLASICO")
        @NotNull(message = "La categoria es obligatoria")
        CategoriaCoctel categoria,

        @Schema(description = "ALCOHOLICA o MOCKTAIL", example = "ALCOHOLICA")
        @NotNull(message = "El tipo de bebida es obligatorio")
        TipoBebida tipo,

        @Schema(description = "Tipo de destilado base. Obligatorio si es ALCOHOLICA, vacio si es MOCKTAIL", example = "Gin")
        @Size(max = 40, message = "El destilado base no puede superar 40 caracteres")
        String destiladoBase,

        @Schema(description = "Si el coctel esta disponible en barra. Por defecto true", example = "true")
        Boolean disponible
) {
}
