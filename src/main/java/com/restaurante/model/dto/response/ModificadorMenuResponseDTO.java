package com.restaurante.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Modificador disponible para un coctel especifico")
public record ModificadorMenuResponseDTO(
        Long id,
        String nombre,
        Double graduacionAlcoholica,
        Double precioExtra,
        @Schema(description = "false si esta agotado o si tiene alcohol y el coctel es Mocktail")
        Boolean habilitado,
        @Schema(example = "NO DISPONIBLE PARA MOCKTAIL")
        String etiqueta,
        @Schema(example = "candado")
        String icono
) {
}
