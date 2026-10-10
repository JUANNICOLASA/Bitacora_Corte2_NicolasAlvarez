package com.restaurante.service;

import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.EventoComanda;
import com.restaurante.model.domain.TipoEvento;

import java.util.List;

public interface EventoComandaService {

    EventoComanda registrar(Long idPedido, TipoEvento tipo, EstadoPedido estado, String descripcion, String usuario);

    List<EventoComanda> listar(Long idPedido);
}
