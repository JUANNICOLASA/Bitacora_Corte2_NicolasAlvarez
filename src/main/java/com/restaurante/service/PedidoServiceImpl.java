package com.restaurante.service;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.PedidoEntityMapper;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Modificador;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.domain.TipoEvento;
import com.restaurante.persistence.entity.ItemPedidoEntity;
import com.restaurante.persistence.entity.PedidoEntity;
import com.restaurante.repository.PedidoRepository;
import com.restaurante.util.TextoUtil;
import com.restaurante.validator.PedidoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
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
    private final EventoComandaService eventoComandaService;

    @Override
    public Pedido crear(Pedido pedido, Authentication usuario) {
        List<ItemPedido> solicitados = pedido.getItems() == null ? List.of() : pedido.getItems();
        List<ItemPedido> items = solicitados.stream()
                .map(this::completarItem)
                .toList();
        pedido.setId(null);
        pedido.setItems(new ArrayList<>(items));
        pedido.setEstado(EstadoPedido.RECIBIDO);
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setUsuario(usuario.getName());
        PedidoEntity guardado = pedidoRepository.save(pedidoEntityMapper.toEntity(pedido));
        log.info("Comanda creada: id={}, mesa={}, items={}", guardado.getId(), guardado.getNumeroMesa(), items.size());
        eventoComandaService.registrar(guardado.getId(), TipoEvento.CREADA, guardado.getEstado(),
                "Comanda creada para la mesa " + guardado.getNumeroMesa() + " con " + items.size() + " items",
                usuario.getName());
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
    public Pedido obtenerPorId(Long id, Authentication usuario) {
        Pedido pedido = pedidoEntityMapper.toDomain(buscarEntidad(id));
        pedidoValidator.validarPropietario(pedido, usuario);
        return pedido;
    }

    @Override
    public Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado, Authentication usuario) {
        PedidoEntity entity = buscarEntidad(id);
        EstadoPedido anterior = entity.getEstado();
        pedidoValidator.validarTransicion(anterior, nuevoEstado);
        log.info("Comanda id={} cambia de {} a {}", id, anterior, nuevoEstado);
        entity.setEstado(nuevoEstado);
        Pedido pedido = pedidoEntityMapper.toDomain(pedidoRepository.save(entity));
        eventoComandaService.registrar(id, TipoEvento.CAMBIO_ESTADO, nuevoEstado,
                "Estado cambiado de " + anterior + " a " + nuevoEstado, usuario.getName());
        return pedido;
    }

    @Override
    public Pedido cambiarDestilado(Long idPedido, Long idItem, String nuevoDestilado, Authentication usuario) {
        PedidoEntity entity = buscarEntidad(idPedido);
        ItemPedidoEntity itemEntity = entity.getItems().stream()
                .filter(i -> i.getId().equals(idItem))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("Item de la comanda " + idPedido, idItem));
        Pedido pedido = pedidoEntityMapper.toDomain(entity);
        ItemPedido item = pedidoEntityMapper.toItemDomain(itemEntity);
        pedidoValidator.validarPropietario(pedido, usuario);
        pedidoValidator.validarCambioDeLicor(pedido, item);
        String anterior = itemEntity.getDestilado();
        log.info("Comanda id={} item={} cambia licor de {} a {}", idPedido, idItem, anterior, nuevoDestilado);
        itemEntity.setDestilado(nuevoDestilado.trim());
        itemEntity.setCambiosDeLicor(itemEntity.getCambiosDeLicor() + 1);
        Pedido actualizado = pedidoEntityMapper.toDomain(pedidoRepository.save(entity));
        eventoComandaService.registrar(idPedido, TipoEvento.CAMBIO_LICOR, actualizado.getEstado(),
                "Licor del item " + idItem + " cambiado de " + anterior + " a " + nuevoDestilado.trim(),
                usuario.getName());
        return actualizado;
    }

    @Override
    public void cancelar(Long id, Authentication usuario) {
        PedidoEntity entity = buscarEntidad(id);
        pedidoValidator.validarTransicion(entity.getEstado(), EstadoPedido.CANCELADO);
        entity.setEstado(EstadoPedido.CANCELADO);
        pedidoRepository.save(entity);
        log.info("Comanda id={} cancelada", id);
        eventoComandaService.registrar(id, TipoEvento.CANCELADA, EstadoPedido.CANCELADO,
                "Comanda cancelada", usuario.getName());
    }

    private PedidoEntity buscarEntidad(Long id) {
        return pedidoRepository.buscarConItems(id).orElseThrow(() -> {
            log.warn("Comanda no encontrada: id={}", id);
            return new RecursoNoEncontradoException(RECURSO, id);
        });
    }
}
