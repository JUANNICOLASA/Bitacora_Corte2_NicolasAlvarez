package com.restaurante.service;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.exception.ReglaNegocioException;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Modificador;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.validator.PedidoValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

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

    private Pedido crearPedidoValido() {
        when(coctelService.obtenerPorId(1L)).thenReturn(negroni);
        return pedidoService.crear(pedidoSolicitado(null));
    }

    @Test
    void crearCompletaItemsYQuedaEnRecibido() {
        when(coctelService.obtenerPorId(1L)).thenReturn(negroni);
        when(modificadorService.obtenerPorId(1L)).thenReturn(shot);

        Pedido creado = pedidoService.crear(pedidoSolicitado(1L));

        assertThat(creado.getId()).isEqualTo(1L);
        assertThat(creado.getEstado()).isEqualTo(EstadoPedido.RECIBIDO);
        assertThat(creado.getFechaCreacion()).isNotNull();
        ItemPedido item = creado.getItems().get(0);
        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getNombreCoctel()).isEqualTo("Negroni");
        assertThat(item.getPrecioUnitario()).isEqualTo(38000.0);
        assertThat(item.getDestilado()).isEqualTo("Tanqueray");
        assertThat(item.getModificadores()).containsExactly(shot);
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
        assertThat(pedidoService.listar()).isEmpty();
    }

    @Test
    void crearConCoctelInexistentePropagaNoEncontrado() {
        when(coctelService.obtenerPorId(1L)).thenThrow(new RecursoNoEncontradoException("Coctel", 1L));

        Pedido solicitado = pedidoSolicitado(null);
        assertThatThrownBy(() -> pedidoService.crear(solicitado))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void crearSinItemsNiModificadoresNoFalla() {
        Pedido vacio = Pedido.builder().numeroMesa(1).items(null).build();
        Pedido creado = pedidoService.crear(vacio);
        assertThat(creado.getItems()).isEmpty();

        when(coctelService.obtenerPorId(1L)).thenReturn(negroni);
        ItemPedido sinModificadores = ItemPedido.builder().idCoctel(1L).cantidad(1)
                .destilado(" ").modificadores(null).build();
        Pedido otro = pedidoService.crear(Pedido.builder().numeroMesa(2)
                .items(new ArrayList<>(List.of(sinModificadores))).build());
        assertThat(otro.getItems().get(0).getModificadores()).isEmpty();
        assertThat(otro.getItems().get(0).getDestilado()).isNull();
    }

    @Test
    void listarPorEstadoYActivos() {
        Pedido primero = crearPedidoValido();
        Pedido segundo = pedidoService.crear(pedidoSolicitado(null));
        segundo.setEstado(EstadoPedido.ENTREGADO);

        assertThat(pedidoService.listar()).hasSize(2);
        assertThat(pedidoService.listarPorEstado(EstadoPedido.RECIBIDO)).containsExactly(primero);
        assertThat(pedidoService.listarActivos()).containsExactly(primero);
    }

    @Test
    void listarVacioDevuelveListaVacia() {
        assertThat(pedidoService.listar()).isEmpty();
        assertThat(pedidoService.listarActivos()).isEmpty();
    }

    @Test
    void obtenerPorIdInexistenteLanzaNoEncontrado() {
        assertThatThrownBy(() -> pedidoService.obtenerPorId(10L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void cambiarEstadoValidaLaTransicion() {
        Pedido creado = crearPedidoValido();

        Pedido resultado = pedidoService.cambiarEstado(creado.getId(), EstadoPedido.EN_PREPARACION);

        assertThat(resultado.getEstado()).isEqualTo(EstadoPedido.EN_PREPARACION);
        verify(pedidoValidator).validarTransicion(EstadoPedido.RECIBIDO, EstadoPedido.EN_PREPARACION);
    }

    @Test
    void cambiarEstadoInvalidoNoModificaLaComanda() {
        Pedido creado = crearPedidoValido();
        doThrow(new ReglaNegocioException("Transicion invalida"))
                .when(pedidoValidator).validarTransicion(EstadoPedido.RECIBIDO, EstadoPedido.ENTREGADO);

        Long id = creado.getId();
        assertThatThrownBy(() -> pedidoService.cambiarEstado(id, EstadoPedido.ENTREGADO))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(creado.getEstado()).isEqualTo(EstadoPedido.RECIBIDO);
    }

    @Test
    void cambiarDestiladoSumaUnCambio() {
        Pedido creado = crearPedidoValido();

        Pedido resultado = pedidoService.cambiarDestilado(creado.getId(), 1L, " Hendrick's ");

        ItemPedido item = resultado.getItems().get(0);
        assertThat(item.getDestilado()).isEqualTo("Hendrick's");
        assertThat(item.getCambiosDeLicor()).isEqualTo(1);
        verify(pedidoValidator).validarCambioDeLicor(creado, item);
    }

    @Test
    void cambiarDestiladoDeItemInexistenteLanzaNoEncontrado() {
        Pedido creado = crearPedidoValido();

        Long id = creado.getId();
        assertThatThrownBy(() -> pedidoService.cambiarDestilado(id, 9L, "Bombay"))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(pedidoValidator, never()).validarCambioDeLicor(any(), any());
    }

    @Test
    void cancelarDejaLaComandaCancelada() {
        Pedido creado = crearPedidoValido();

        pedidoService.cancelar(creado.getId());

        assertThat(creado.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
        verify(pedidoValidator).validarTransicion(EstadoPedido.RECIBIDO, EstadoPedido.CANCELADO);
    }
}
