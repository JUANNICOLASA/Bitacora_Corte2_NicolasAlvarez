package com.restaurante.service;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.PedidoEntityMapper;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Modificador;
import com.restaurante.model.domain.Pedido;
import com.restaurante.persistence.entity.ItemPedidoEntity;
import com.restaurante.persistence.entity.PedidoEntity;
import com.restaurante.repository.PedidoRepository;
import com.restaurante.util.TextoUtil;
import com.restaurante.validator.PedidoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private static final String RECURSO = "Pedido";
    private static final EnumSet<EstadoPedido> ESTADOS_FINALES =
            EnumSet.of(EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO);

    private final CoctelService coctelService;
    private final ModificadorService modificadorService;
    private final PedidoValidator pedidoValidator;
    private final PedidoRepository pedidoRepository;
    private final PedidoEntityMapper pedidoEntityMapper;

    @Override
    public Pedido crear(Pedido pedido) {
        List<ItemPedido> solicitados = pedido.getItems() == null ? List.of() : pedido.getItems();
        List<ItemPedido> items = solicitados.stream()
                .map(this::completarItem)
                .toList();
        pedido.setId(null);
        pedido.setItems(new ArrayList<>(items));
        pedido.setEstado(EstadoPedido.RECIBIDO);
        pedido.setFechaCreacion(LocalDateTime.now());
        PedidoEntity guardado = pedidoRepository.save(pedidoEntityMapper.toEntity(pedido));
        log.info("Comanda creada: id={}, mesa={}, items={}", guardado.getId(), guardado.getNumeroMesa(), items.size());
        return pedidoEntityMapper.toDomain(guardado);
    }

    private ItemPedido completarItem(ItemPedido item) {
        Coctel coctel = coctelService.obtenerPorId(item.getIdCoctel());
        pedidoValidator.validarCoctelDisponible(coctel);
        pedidoValidator.validarTrazabilidad(coctel, item.getDestilado());

        List<Modificador> solicitados = item.getModificadores() == null ? List.of() : item.getModificadores();
        List<Modificador> modificadores = solicitados.stream()
                .map(m -> obtenerModificadorValido(coctel, m.getId()))
                .toList();

        item.setId(null);
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
    @Transactional(readOnly = true)
    public List<Pedido> listar() {
        return pedidoRepository.findAllByOrderByIdAsc().stream()
                .map(pedidoEntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPorEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstadoOrderByIdAsc(estado).stream()
                .map(pedidoEntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarActivos() {
        return pedidoRepository.findByEstadoNotInOrderByFechaCreacionAsc(ESTADOS_FINALES).stream()
                .map(pedidoEntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Pedido obtenerPorId(Long id) {
        return pedidoEntityMapper.toDomain(buscarEntidad(id));
    }

    @Override
    public Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        PedidoEntity entity = buscarEntidad(id);
        pedidoValidator.validarTransicion(entity.getEstado(), nuevoEstado);
        log.info("Comanda id={} cambia de {} a {}", id, entity.getEstado(), nuevoEstado);
        entity.setEstado(nuevoEstado);
        return pedidoEntityMapper.toDomain(pedidoRepository.save(entity));
    }

    @Override
    public Pedido cambiarDestilado(Long idPedido, Long idItem, String nuevoDestilado) {
        PedidoEntity entity = buscarEntidad(idPedido);
        ItemPedidoEntity itemEntity = entity.getItems().stream()
                .filter(i -> i.getId().equals(idItem))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("Item de la comanda " + idPedido, idItem));
        Pedido pedido = pedidoEntityMapper.toDomain(entity);
        ItemPedido item = pedidoEntityMapper.toItemDomain(itemEntity);
        pedidoValidator.validarCambioDeLicor(pedido, item);
        log.info("Comanda id={} item={} cambia licor de {} a {}", idPedido, idItem, itemEntity.getDestilado(), nuevoDestilado);
        itemEntity.setDestilado(nuevoDestilado.trim());
        itemEntity.setCambiosDeLicor(itemEntity.getCambiosDeLicor() + 1);
        return pedidoEntityMapper.toDomain(pedidoRepository.save(entity));
    }

    @Override
    public void cancelar(Long id) {
        PedidoEntity entity = buscarEntidad(id);
        pedidoValidator.validarTransicion(entity.getEstado(), EstadoPedido.CANCELADO);
        entity.setEstado(EstadoPedido.CANCELADO);
        pedidoRepository.save(entity);
        log.info("Comanda id={} cancelada", id);
    }

    private PedidoEntity buscarEntidad(Long id) {
        return pedidoRepository.buscarConItems(id).orElseThrow(() -> {
            log.warn("Comanda no encontrada: id={}", id);
            return new RecursoNoEncontradoException(RECURSO, id);
        });
    }
}
