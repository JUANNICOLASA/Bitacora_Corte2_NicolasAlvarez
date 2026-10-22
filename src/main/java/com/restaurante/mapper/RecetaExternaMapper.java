package com.restaurante.mapper;

import com.restaurante.client.dto.BebidaExternaDTO;
import com.restaurante.model.domain.RecetaExterna;
import com.restaurante.model.dto.response.RecetaExternaResponseDTO;
import com.restaurante.util.TextoUtil;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RecetaExternaMapper {

    String FUENTE = "TheCocktailDB";

    RecetaExternaResponseDTO toResponse(RecetaExterna receta);

    default RecetaExterna toDomain(BebidaExternaDTO bebida) {
        if (bebida == null) {
            return null;
        }
        String instrucciones = TextoUtil.estaVacio(bebida.getInstruccionesEs())
                ? bebida.getInstrucciones()
                : bebida.getInstruccionesEs();
        return RecetaExterna.builder()
                .nombre(bebida.getNombre())
                .vaso(bebida.getVaso())
                .instrucciones(instrucciones)
                .ingredientes(bebida.ingredientes())
                .imagen(bebida.getImagen())
                .fuente(FUENTE)
                .build();
    }
}
