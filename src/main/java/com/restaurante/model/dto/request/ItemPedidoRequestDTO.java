package com.restaurante.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Coctel solicitado dentro de una comanda")
public record ItemPedidoRequestDTO(

        @Schema(description = "Id del coctel", example = "2")
        @NotNull(message = "El id del coctel es obligatorio")
        Long idCoctel,

        @Schema(description = "Cantidad de unidades", example = "2")
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad minima es 1")
        @Max(value = 20, message = "La cantidad maxima es 20")
        Integer cantidad,

        @Schema(description = "Marca o tipo exacto del destilado. Obligatorio para cocteles alcoholicos",
                example = "Tanqueray London Dry")
        @Size(max = 60, message = "El destilado no puede superar 60 caracteres")
        String destilado,

        @Schema(description = "Ids de los modificadores a aplicar", example = "[3]")
        List<Long> idsModificadores
) {
}
