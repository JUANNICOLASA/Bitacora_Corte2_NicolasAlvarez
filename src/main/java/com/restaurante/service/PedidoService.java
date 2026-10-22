package com.restaurante.service;

import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.Pedido;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface PedidoService {

    Pedido crear(Pedido pedido, Authentication usuario);

    List<Pedido> listar();

    List<Pedido> listarPorEstado(EstadoPedido estado);

    List<Pedido> listarActivos();

    List<Pedido> listarMios(Authentication usuario);

    Pedido obtenerPorId(Long id, Authentication usuario);

    Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado, Authentication usuario);

    Pedido cambiarDestilado(Long idPedido, Long idItem, String nuevoDestilado, Authentication usuario);

    void cancelar(Long id, Authentication usuario);
}
