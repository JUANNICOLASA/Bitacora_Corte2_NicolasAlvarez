package com.restaurante.mapper;

import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Modificador;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.model.dto.request.ItemPedidoRequestDTO;
import com.restaurante.model.dto.request.PedidoRequestDTO;
import com.restaurante.model.dto.response.PedidoResponseDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PedidoMapperTest {

    private final PedidoMapper mapper = Mappers.getMapper(PedidoMapper.class);

    @Test
    void requestADominioSoloConIds() {
        PedidoRequestDTO request = new PedidoRequestDTO(7, List.of(
                new ItemPedidoRequestDTO(2L, 2, "Tanqueray", List.of(1L, 3L))));

        Pedido pedido = mapper.toDomain(request);

        assertThat(pedido.getId()).isNull();
        assertThat(pedido.getEstado()).isNull();
        assertThat(pedido.getNumeroMesa()).isEqualTo(7);
        ItemPedido item = pedido.getItems().get(0);
        assertThat(item.getIdCoctel()).isEqualTo(2L);
        assertThat(item.getCantidad()).isEqualTo(2);
        assertThat(item.getDestilado()).isEqualTo("Tanqueray");
        assertThat(item.getModificadores()).extracting(Modificador::getId).containsExactly(1L, 3L);
    }

    @Test
    void dominioAResponseConTotalesYNombresDeModificadores() {
        ItemPedido item = ItemPedido.builder().id(1L).idCoctel(2L).nombreCoctel("Negroni")
                .tipo(TipoBebida.ALCOHOLICA).precioUnitario(38000.0).cantidad(2).destilado("Tanqueray")
                .modificadores(List.of(Modificador.builder().nombre("Shot extra").precioExtra(9000.0).build()))
                .cambiosDeLicor(0).build();
        Pedido pedido = Pedido.builder().id(1L).numeroMesa(7).estado(EstadoPedido.RECIBIDO)
                .fechaCreacion(LocalDateTime.now()).items(List.of(item)).build();

        PedidoResponseDTO response = mapper.toResponse(pedido);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.estado()).isEqualTo(EstadoPedido.RECIBIDO);
        assertThat(response.total()).isEqualTo(94000.0);
        assertThat(response.items().get(0).subtotal()).isEqualTo(94000.0);
        assertThat(response.items().get(0).modificadores()).containsExactly("Shot extra");
        assertThat(mapper.toResponseList(List.of(pedido))).hasSize(1);
    }

    @Test
    void metodosAuxiliaresYNulos() {
        assertThat(mapper.modificadorDesdeId(null)).isNull();
        assertThat(mapper.modificadorDesdeId(4L).getId()).isEqualTo(4L);
        assertThat(mapper.nombreModificador(null)).isNull();
        assertThat(mapper.toDomain(null)).isNull();
        assertThat(mapper.toResponse(null)).isNull();
        assertThat(mapper.toItemDomain(null)).isNull();
        assertThat(mapper.toItemResponse(null)).isNull();
        assertThat(mapper.toResponseList(null)).isNull();
    }
}
