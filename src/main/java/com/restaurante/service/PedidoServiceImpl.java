package com.restaurante.service;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Modificador;
import com.restaurante.model.domain.Pedido;
import com.restaurante.util.TextoUtil;
import com.restaurante.validator.PedidoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementacion en memoria del servicio de comandas.
 * Reutiliza CoctelService y ModificadorService (via interfaz) para validar la carta.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private static final String RECURSO = "Pedido";

    private final CoctelService coctelService;
    private final ModificadorService modificadorService;
    private final PedidoValidator pedidoValidator;

    private final Map<Long, Pedido> pedidos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    @Override
    public Pedido crear(Pedido pedido) {
        List<ItemPedido> solicitados = pedido.getItems() == null ? List.of() : pedido.getItems();
        List<ItemPedido> items = new ArrayList<>();
        for (int i = 0; i < solicitados.size(); i++) {
            items.add(completarItem(solicitados.get(i), (long) i + 1));
        }
        pedido.setItems(items);
        pedido.setId(secuencia.incrementAndGet());
        pedido.setEstado(EstadoPedido.RECIBIDO);
        pedido.setFechaCreacion(LocalDateTime.now());
        pedidos.put(pedido.getId(), pedido);
        log.info("Comanda creada: id={}, mesa={}, items={}", pedido.getId(), pedido.getNumeroMesa(), items.size());
        return pedido;
    }

    private ItemPedido completarItem(ItemPedido item, Long idItem) {
        Coctel coctel = coctelService.obtenerPorId(item.getIdCoctel());
        pedidoValidator.validarCoctelDisponible(coctel);
        pedidoValidator.validarTrazabilidad(coctel, item.getDestilado());

        List<Modificador> solicitados = item.getModificadores() == null ? List.of() : item.getModificadores();
        List<Modificador> modificadores = solicitados.stream()
                .map(m -> obtenerModificadorValido(coctel, m.getId()))
                .toList();

        item.setId(idItem);
        item.setNombreCoctel(coctel.getNombre());
        item.setTipo(coctel.getTipo());
        item.setPrecioUnitario(coctel.getPrecio());
        item.setDestilado(TextoUtil.estaVacio(item.getDestilado()) ? null : item.getDestilado().trim());
        item.setModificadores(new ArrayList<>(modificadores));
        item.setCambiosDeLicor(0);
        return item;
    }

    private Modificador obtenerModificadorValido(Coctel coctel, Long idModificador) {
        Modificador modificador = modificadorService.obtenerPorId(idModificador);
        pedidoValidator.validarModificador(coctel, modificador);
        return modificador;
    }

    @Override
    public List<Pedido> listar() {
        return pedidos.values().stream()
                .sorted(Comparator.comparing(Pedido::getId))
                .toList();
    }

    @Override
    public List<Pedido> listarPorEstado(EstadoPedido estado) {
        return listar().stream()
                .filter(p -> p.getEstado() == estado)
                .toList();
    }

    @Override
    public List<Pedido> listarActivos() {
        return listar().stream()
                .filter(Pedido::estaActivo)
                .sorted(Comparator.comparing(Pedido::getFechaCreacion))
                .toList();
    }

    @Override
    public Pedido obtenerPorId(Long id) {
        Pedido pedido = pedidos.get(id);
        if (pedido == null) {
            log.warn("Comanda no encontrada: id={}", id);
            throw new RecursoNoEncontradoException(RECURSO, id);
        }
        return pedido;
    }

    @Override
    public Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = obtenerPorId(id);
        pedidoValidator.validarTransicion(pedido.getEstado(), nuevoEstado);
        log.info("Comanda id={} cambia de {} a {}", id, pedido.getEstado(), nuevoEstado);
        pedido.setEstado(nuevoEstado);
        return pedido;
    }

    @Override
    public Pedido cambiarDestilado(Long idPedido, Long idItem, String nuevoDestilado) {
        Pedido pedido = obtenerPorId(idPedido);
        ItemPedido item = pedido.getItems().stream()
                .filter(i -> i.getId().equals(idItem))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("Item de la comanda " + idPedido, idItem));
        pedidoValidator.validarCambioDeLicor(pedido, item);
        log.info("Comanda id={} item={} cambia licor de {} a {}", idPedido, idItem, item.getDestilado(), nuevoDestilado);
        item.setDestilado(nuevoDestilado.trim());
        item.setCambiosDeLicor(item.getCambiosDeLicor() + 1);
        return pedido;
    }

    @Override
    public void cancelar(Long id) {
        Pedido pedido = obtenerPorId(id);
        pedidoValidator.validarTransicion(pedido.getEstado(), EstadoPedido.CANCELADO);
        pedido.setEstado(EstadoPedido.CANCELADO);
        log.info("Comanda id={} cancelada", id);
    }
}
