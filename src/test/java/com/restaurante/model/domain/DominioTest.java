package com.restaurante.model.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DominioTest {

    @Test
    void estadoPedidoSigueElFlujoDelKds() {
        assertThat(EstadoPedido.RECIBIDO.siguiente()).isEqualTo(EstadoPedido.EN_PREPARACION);
        assertThat(EstadoPedido.EN_PREPARACION.siguiente()).isEqualTo(EstadoPedido.LISTO);
        assertThat(EstadoPedido.LISTO.siguiente()).isEqualTo(EstadoPedido.ENTREGADO);
        assertThat(EstadoPedido.ENTREGADO.siguiente()).isNull();
        assertThat(EstadoPedido.CANCELADO.siguiente()).isNull();
        assertThat(EstadoPedido.ENTREGADO.esFinal()).isTrue();
        assertThat(EstadoPedido.LISTO.esFinal()).isFalse();
    }

    @Test
    void itemPedidoSumaPrecioYModificadoresPorCantidad() {
        ItemPedido item = ItemPedido.builder()
                .precioUnitario(38000.0)
                .cantidad(2)
                .modificadores(List.of(
                        Modificador.builder().precioExtra(9000.0).build(),
                        Modificador.builder().precioExtra(null).build()))
                .build();

        assertThat(item.subtotal()).isEqualTo(94000.0);
    }

    @Test
    void itemPedidoSinDatosTieneSubtotalCero() {
        ItemPedido item = ItemPedido.builder().modificadores(null).build();
        assertThat(item.subtotal()).isZero();
    }

    @Test
    void pedidoCalculaTotalYEstado() {
        Pedido pedido = Pedido.builder()
                .estado(EstadoPedido.RECIBIDO)
                .items(List.of(
                        ItemPedido.builder().precioUnitario(20000.0).cantidad(1).build(),
                        ItemPedido.builder().precioUnitario(10000.0).cantidad(3).build()))
                .build();

        assertThat(pedido.calcularTotal()).isEqualTo(50000.0);
        assertThat(pedido.puedeModificarse()).isTrue();
        assertThat(pedido.estaActivo()).isTrue();

        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedido.setItems(null);
        assertThat(pedido.puedeModificarse()).isFalse();
        assertThat(pedido.estaActivo()).isFalse();
        assertThat(pedido.calcularTotal()).isZero();
    }

    @Test
    void modificadorConAlcoholNoEsCompatibleConMocktail() {
        Coctel mocktail = Coctel.builder().tipo(TipoBebida.MOCKTAIL).disponible(true).build();
        Coctel alcoholico = Coctel.builder().tipo(TipoBebida.ALCOHOLICA).disponible(true).build();
        Modificador shot = Modificador.builder().graduacionAlcoholica(40.0).disponible(true).build();
        Modificador jarabe = Modificador.builder().graduacionAlcoholica(0.0).disponible(true).build();
        Modificador agotado = Modificador.builder().graduacionAlcoholica(null).disponible(false).build();

        assertThat(shot.esCompatibleCon(mocktail)).isFalse();
        assertThat(shot.esCompatibleCon(alcoholico)).isTrue();
        assertThat(jarabe.esCompatibleCon(mocktail)).isTrue();
        assertThat(agotado.esCompatibleCon(alcoholico)).isFalse();
        assertThat(agotado.esAlcoholico()).isFalse();
        assertThat(mocktail.esMocktail()).isTrue();
        assertThat(alcoholico.estaDisponible()).isTrue();
    }
}
