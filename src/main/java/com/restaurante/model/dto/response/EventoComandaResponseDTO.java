package com.restaurante.model.dto.response;

import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.TipoEvento;

import java.time.LocalDateTime;

public record EventoComandaResponseDTO(
        String id,
        Long idPedido,
        TipoEvento tipo,
        String descripcion,
        EstadoPedido estado,
        String usuario,
        LocalDateTime fecha
) {
}
