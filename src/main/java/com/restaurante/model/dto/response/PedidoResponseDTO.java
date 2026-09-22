package com.restaurante.model.dto.response;

import com.restaurante.model.domain.EstadoPedido;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDTO(
        Long id,
        Integer numeroMesa,
        EstadoPedido estado,
        LocalDateTime fechaCreacion,
        List<ItemPedidoResponseDTO> items,
        Double total
) {
}
