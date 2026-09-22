package com.restaurante.model.dto.response;

import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.TipoBebida;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Coctel tal como lo ve el cliente en la carta digital")
public record MenuItemResponseDTO(
        Long id,
        String nombre,
        String descripcion,
        Double precio,
        CategoriaCoctel categoria,
        TipoBebida tipo,
        String destiladoBase,
        Boolean disponible,
        @Schema(description = "Texto que acompana al color", example = "AGOTADO EN BARRA")
        String etiqueta,
        @Schema(description = "Icono que acompana al color", example = "candado")
        String icono,
        @Schema(description = "Si el boton de seleccion esta activo")
        Boolean seleccionable
) {
}
