package com.restaurante.service;

import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.Pedido;

import java.util.List;

/**
 * Contrato del servicio de comandas (tablero del bartender - KDS).
 */
public interface PedidoService {

    Pedido crear(Pedido pedido);

    List<Pedido> listar();

    List<Pedido> listarPorEstado(EstadoPedido estado);

    List<Pedido> listarActivos();

    Pedido obtenerPorId(Long id);

    Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado);

    Pedido cambiarDestilado(Long idPedido, Long idItem, String nuevoDestilado);

    void cancelar(Long id);
}
