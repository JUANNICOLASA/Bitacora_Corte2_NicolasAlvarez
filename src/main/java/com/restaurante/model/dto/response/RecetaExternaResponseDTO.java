package com.restaurante.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Receta de referencia del coctel obtenida de un proveedor externo")
public record RecetaExternaResponseDTO(
        @Schema(example = "Negroni") String nombre,
        @Schema(example = "Old-fashioned glass") String vaso,
        String instrucciones,
        List<String> ingredientes,
        String imagen,
        @Schema(example = "TheCocktailDB") String fuente
) {
}
