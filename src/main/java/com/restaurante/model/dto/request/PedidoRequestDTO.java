package com.restaurante.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "Comanda que se envia al tablero del bartender")
public record PedidoRequestDTO(

        @Schema(description = "Numero de la mesa", example = "7")
        @NotNull(message = "El numero de mesa es obligatorio")
        @Positive(message = "El numero de mesa debe ser mayor a 0")
        Integer numeroMesa,

        @NotEmpty(message = "La comanda debe tener al menos un coctel")
        @Valid
        List<ItemPedidoRequestDTO> items
) {
}
