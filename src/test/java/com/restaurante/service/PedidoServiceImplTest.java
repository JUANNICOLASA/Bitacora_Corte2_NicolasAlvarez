package com.restaurante.service;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.exception.ReglaNegocioException;
import com.restaurante.mapper.PedidoEntityMapper;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Modificador;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.persistence.entity.ItemPedidoEntity;
import com.restaurante.persistence.entity.PedidoEntity;
import com.restaurante.repository.PedidoRepository;
import com.restaurante.validator.PedidoValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private CoctelService coctelService;

    @Mock
    private ModificadorService modificadorService;

    @Mock
    private PedidoValidator pedidoValidator;

    @Mock
    private PedidoRepository pedidoRepository;

    @Spy
    private PedidoEntityMapper pedidoEntityMapper = Mappers.getMapper(PedidoEntityMapper.class);

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private final Coctel negroni = Coctel.builder().id(1L).nombre("Negroni").precio(38000.0)
            .tipo(TipoBebida.ALCOHOLICA).destiladoBase("Gin").disponible(true).build();
    private final Modificador shot = Modificador.builder().id(1L).nombre("Shot extra")
            .graduacionAlcoholica(40.0).precioExtra(9000.0).disponible(true).build();

    private Pedido pedidoSolicitado(Long idModificador) {
        List<Modificador> modificadores = new ArrayList<>();
        if (idModificador != null) {
            modificadores.add(Modificador.builder().id(idModificador).build());
        }
        ItemPedido item = ItemPedido.builder()
                .idCoctel(1L)
                .cantidad(2)
                .destilado("  Tanqueray  ")
                .modificadores(modificadores)
                .build();
        return Pedido.builder().numeroMesa(7).items(new ArrayList<>(List.of(item))).build();
    }

    private void simularGuardado() {
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(invocacion -> {
            PedidoEntity entity = invocacion.getArgument(0);
            entity.setId(1L);
            long idItem = 1;
            for (ItemPedidoEntity item : entity.getItems()) {
                item.setId(idItem++);
            }
            return entity;
        });
    }

    private PedidoEntity pedidoGuardado(EstadoPedido estado, TipoBebida tipo, int cambiosDeLicor) {
        ItemPedido item = ItemPedido.builder().id(1L).idCoctel(1L).nombreCoctel("Negroni").tipo(tipo)
                .precioUnitario(38000.0).cantidad(1).destilado("Tanqueray").cambiosDeLicor(cambiosDeLicor).build();
        Pedido pedido = Pedido.builder().id(1L).numeroMesa(7).estado(estado)
                .fechaCreacion(LocalDateTime.now()).items(new ArrayList<>(List.of(item))).build();
        return pedidoEntityMapper.toEntity(pedido);
    }

    @Test
    void crearCompletaItemsGuardaYQuedaEnRecibido() {
        when(coctelService.obtenerPorId(1L)).thenReturn(negroni);
        when(modificadorService.obtenerPorId(1L)).thenReturn(shot);
        simularGuardado();

        Pedido creado = pedidoService.crear(pedidoSolicitado(1L));

        assertThat(creado.getId()).isEqualTo(1L);
        assertThat(creado.getEstado()).isEqualTo(EstadoPedido.RECIBIDO);
        assertThat(creado.getFechaCreacion()).isNotNull();
        ItemPedido item = creado.getItems().get(0);
        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getNombreCoctel()).isEqualTo("Negroni");
        assertThat(item.getPrecioUnitario()).isEqualTo(38000.0);
        assertThat(item.getDestilado()).isEqualTo("Tanqueray");
        assertThat(item.getModificadores()).extracting(Modificador::getNombre).containsExactly("Shot extra");
        assertThat(creado.calcularTotal()).isEqualTo(94000.0);
        verify(pedidoValidator).validarCoctelDisponible(negroni);
        verify(pedidoValidator).validarTrazabilidad(negroni, "  Tanqueray  ");
        verify(pedidoValidator).validarModificador(negroni, shot);
    }

    @Test
    void crearConCoctelAgotadoNoGuardaLaComanda() {
        when(coctelService.obtenerPorId(1L)).thenReturn(negroni);
        doThrow(new ReglaNegocioException("AGOTADO EN BARRA"))
                .when(pedidoValidator).validarCoctelDisponible(negroni);

        Pedido solicitado = pedidoSolicitado(null);
        assertThatThrownBy(() -> pedidoService.crear(solicitado))
                .isInstanceOf(ReglaNegocioException.class);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void crearConCoctelInexistentePropagaNoEncontrado() {
        when(coctelService.obtenerPorId(1L)).thenThrow(new RecursoNoEncontradoException("Coctel", 1L));

        Pedido solicitado = pedidoSolicitado(null);
        assertThatThrownBy(() -> pedidoService.crear(solicitado))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void crearSinModificadoresNiDestilado() {
        when(coctelService.obtenerPorId(1L)).thenReturn(negroni);
        simularGuardado();
        ItemPedido item = ItemPedido.builder().idCoctel(1L).cantidad(1).destilado(" ").modificadores(null).build();

        Pedido creado = pedidoService.crear(Pedido.builder().numeroMesa(2)
                .items(new ArrayList<>(List.of(item))).build());

        assertThat(creado.getItems().get(0).getModificadores()).isEmpty();
        assertThat(creado.getItems().get(0).getDestilado()).isNull();
    }

    @Test
    void crearSinItemsGuardaComandaVacia() {
        simularGuardado();

        Pedido creado = pedidoService.crear(Pedido.builder().numeroMesa(1).items(null).build());

        assertThat(creado.getItems()).isEmpty();
    }

    @Test
    void listarPorEstadoYActivos() {
        PedidoEntity recibido = pedidoGuardado(EstadoPedido.RECIBIDO, TipoBebida.ALCOHOLICA, 0);
        when(pedidoRepository.findAllByOrderByIdAsc()).thenReturn(List.of(recibido));
        when(pedidoRepository.findByEstadoOrderByIdAsc(EstadoPedido.RECIBIDO)).thenReturn(List.of(recibido));
        when(pedidoRepository.findByEstadoNotInOrderByFechaCreacionAsc(any())).thenReturn(List.of(recibido));

        assertThat(pedidoService.listar()).hasSize(1);
        assertThat(pedidoService.listarPorEstado(EstadoPedido.RECIBIDO)).extracting(Pedido::getId).containsExactly(1L);
        assertThat(pedidoService.listarActivos()).extracting(Pedido::getEstado).containsExactly(EstadoPedido.RECIBIDO);
    }

    @Test
    void listarVacioDevuelveListaVacia() {
        when(pedidoRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

        assertThat(pedidoService.listar()).isEmpty();
    }

    @Test
    void obtenerPorIdInexistenteLanzaNoEncontrado() {
        when(pedidoRepository.buscarConItems(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.obtenerPorId(10L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void obtenerPorIdExistente() {
        PedidoEntity entity = pedidoGuardado(EstadoPedido.RECIBIDO, TipoBebida.ALCOHOLICA, 0);
        when(pedidoRepository.buscarConItems(1L)).thenReturn(Optional.of(entity));

        Pedido pedido = pedidoService.obtenerPorId(1L);

        assertThat(pedido.getItems()).extracting(ItemPedido::getIdCoctel).containsExactly(1L);
    }

    @Test
    void cambiarEstadoValidaLaTransicionYGuarda() {
        PedidoEntity entity = pedidoGuardado(EstadoPedido.RECIBIDO, TipoBebida.ALCOHOLICA, 0);
        when(pedidoRepository.buscarConItems(1L)).thenReturn(Optional.of(entity));
        when(pedidoRepository.save(entity)).thenReturn(entity);

        Pedido resultado = pedidoService.cambiarEstado(1L, EstadoPedido.EN_PREPARACION);

        assertThat(resultado.getEstado()).isEqualTo(EstadoPedido.EN_PREPARACION);
        verify(pedidoValidator).validarTransicion(EstadoPedido.RECIBIDO, EstadoPedido.EN_PREPARACION);
    }

    @Test
    void cambiarEstadoInvalidoNoGuarda() {
        PedidoEntity entity = pedidoGuardado(EstadoPedido.RECIBIDO, TipoBebida.ALCOHOLICA, 0);
        when(pedidoRepository.buscarConItems(1L)).thenReturn(Optional.of(entity));
        doThrow(new ReglaNegocioException("Transicion invalida"))
                .when(pedidoValidator).validarTransicion(EstadoPedido.RECIBIDO, EstadoPedido.ENTREGADO);

        assertThatThrownBy(() -> pedidoService.cambiarEstado(1L, EstadoPedido.ENTREGADO))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(entity.getEstado()).isEqualTo(EstadoPedido.RECIBIDO);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void cambiarDestiladoSumaUnCambio() {
        PedidoEntity entity = pedidoGuardado(EstadoPedido.RECIBIDO, TipoBebida.ALCOHOLICA, 0);
        when(pedidoRepository.buscarConItems(1L)).thenReturn(Optional.of(entity));
        when(pedidoRepository.save(entity)).thenReturn(entity);

        Pedido resultado = pedidoService.cambiarDestilado(1L, 1L, " Hendrick's ");

        ItemPedido item = resultado.getItems().get(0);
        assertThat(item.getDestilado()).isEqualTo("Hendrick's");
        assertThat(item.getCambiosDeLicor()).isEqualTo(1);
        verify(pedidoValidator).validarCambioDeLicor(any(Pedido.class), any(ItemPedido.class));
    }

    @Test
    void cambiarDestiladoDeItemInexistenteLanzaNoEncontrado() {
        PedidoEntity entity = pedidoGuardado(EstadoPedido.RECIBIDO, TipoBebida.ALCOHOLICA, 0);
        when(pedidoRepository.buscarConItems(1L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> pedidoService.cambiarDestilado(1L, 9L, "Bombay"))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(pedidoValidator, never()).validarCambioDeLicor(any(), any());
    }

    @Test
    void cancelarDejaLaComandaCancelada() {
        PedidoEntity entity = pedidoGuardado(EstadoPedido.RECIBIDO, TipoBebida.ALCOHOLICA, 0);
        when(pedidoRepository.buscarConItems(1L)).thenReturn(Optional.of(entity));

        pedidoService.cancelar(1L);

        assertThat(entity.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
        verify(pedidoValidator).validarTransicion(EstadoPedido.RECIBIDO, EstadoPedido.CANCELADO);
        verify(pedidoRepository).save(entity);
    }
}
