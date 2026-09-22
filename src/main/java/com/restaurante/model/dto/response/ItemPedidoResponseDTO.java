package com.restaurante.model.dto.response;

import com.restaurante.model.domain.TipoBebida;

import java.util.List;

public record ItemPedidoResponseDTO(
        Long id,
        Long idCoctel,
        String nombreCoctel,
        TipoBebida tipo,
        Double precioUnitario,
        Integer cantidad,
        String destilado,
        List<String> modificadores,
        Integer cambiosDeLicor,
        Double subtotal
) {
}
