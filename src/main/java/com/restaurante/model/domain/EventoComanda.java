package com.restaurante.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoComanda {

    private String id;
    private Long idPedido;
    private TipoEvento tipo;
    private String descripcion;
    private EstadoPedido estado;
    private String usuario;
    private LocalDateTime fecha;
}
