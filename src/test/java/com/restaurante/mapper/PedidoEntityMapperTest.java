package com.restaurante.mapper;

import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Modificador;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.persistence.entity.ItemModificadorEntity;
import com.restaurante.persistence.entity.ItemPedidoEntity;
import com.restaurante.persistence.entity.PedidoEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PedidoEntityMapperTest {

    private final PedidoEntityMapper mapper = Mappers.getMapper(PedidoEntityMapper.class);

    private Pedido pedido() {
        Modificador shot = Modificador.builder().id(4L).nombre("Shot extra")
                .graduacionAlcoholica(40.0).precioExtra(9000.0).disponible(true).build();
        ItemPedido item = ItemPedido.builder().idCoctel(2L).nombreCoctel("Negroni").tipo(TipoBebida.ALCOHOLICA)
                .precioUnitario(38000.0).cantidad(2).destilado("Tanqueray")
                .modificadores(new ArrayList<>(List.of(shot))).cambiosDeLicor(0).build();
        return Pedido.builder().numeroMesa(7).estado(EstadoPedido.RECIBIDO)
                .fechaCreacion(LocalDateTime.now()).items(new ArrayList<>(List.of(item))).build();
    }

    @Test
    void dominioAEntidadEnlazaLasRelaciones() {
        PedidoEntity entity = mapper.toEntity(pedido());

        ItemPedidoEntity item = entity.getItems().get(0);
        ItemModificadorEntity modificador = item.getModificadores().get(0);
        assertThat(item.getPedido()).isSameAs(entity);
        assertThat(item.getCoctel().getId()).isEqualTo(2L);
        assertThat(modificador.getItem()).isSameAs(item);
        assertThat(modificador.getModificador().getId()).isEqualTo(4L);
        assertThat(modificador.getPrecioExtra()).isEqualTo(9000.0);
    }

    @Test
    void entidadADominioConservaPreciosCongelados() {
        PedidoEntity entity = mapper.toEntity(pedido());
        entity.getItems().get(0).getModificadores().get(0).getModificador().setPrecioExtra(12000.0);

        Pedido resultado = mapper.toDomain(entity);

        ItemPedido item = resultado.getItems().get(0);
        assertThat(item.getIdCoctel()).isEqualTo(2L);
        assertThat(item.getModificadores().get(0).getPrecioExtra()).isEqualTo(9000.0);
        assertThat(resultado.calcularTotal()).isEqualTo(94000.0);
    }

    @Test
    void listasNulasSeInicializan() {
        Pedido sinItems = Pedido.builder().numeroMesa(1).items(null).build();
        PedidoEntity entity = mapper.toEntity(sinItems);
        assertThat(entity.getItems()).isEmpty();

        ItemPedido item = ItemPedido.builder().idCoctel(null).modificadores(null).build();
        Pedido conItem = Pedido.builder().numeroMesa(1).items(new ArrayList<>(List.of(item))).build();
        PedidoEntity otro = mapper.toEntity(conItem);
        assertThat(otro.getItems().get(0).getModificadores()).isEmpty();
        assertThat(otro.getItems().get(0).getCoctel()).isNull();
    }

    @Test
    void nulos() {
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toDomain(null)).isNull();
        assertThat(mapper.toItemModificador(null)).isNull();
        assertThat(mapper.toModificadorDomain(null)).isNull();
        assertThat(mapper.coctelDesdeId(null)).isNull();
    }

    @Test
    void modificadorSinPrecioQuedaEnCero() {
        ItemModificadorEntity entity = mapper.toItemModificador(Modificador.builder().id(1L).build());
        assertThat(entity.getPrecioExtra()).isZero();
    }
}
