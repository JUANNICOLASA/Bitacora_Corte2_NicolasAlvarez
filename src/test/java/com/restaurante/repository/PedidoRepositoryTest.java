package com.restaurante.repository;

import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.persistence.entity.CoctelEntity;
import com.restaurante.persistence.entity.ItemModificadorEntity;
import com.restaurante.persistence.entity.ItemPedidoEntity;
import com.restaurante.persistence.entity.ModificadorEntity;
import com.restaurante.persistence.entity.PedidoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PedidoRepositoryTest {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private CoctelEntity negroni;
    private CoctelEntity martini;
    private PedidoEntity recibido;

    @BeforeEach
    void setUp() {
        negroni = entityManager.persist(coctel("Negroni"));
        martini = entityManager.persist(coctel("Martini Seco"));
        ModificadorEntity shot = new ModificadorEntity();
        shot.setNombre("Shot extra");
        shot.setGraduacionAlcoholica(40.0);
        shot.setPrecioExtra(9000.0);
        shot.setDisponible(true);
        entityManager.persist(shot);

        recibido = pedidoRepository.save(pedido(EstadoPedido.RECIBIDO, negroni, shot));
        pedidoRepository.save(pedido(EstadoPedido.ENTREGADO, negroni, null));
        entityManager.flush();
        entityManager.clear();
    }

    private CoctelEntity coctel(String nombre) {
        CoctelEntity entity = new CoctelEntity();
        entity.setNombre(nombre);
        entity.setPrecio(38000.0);
        entity.setCategoria(CategoriaCoctel.CLASICO);
        entity.setTipo(TipoBebida.ALCOHOLICA);
        entity.setDestiladoBase("Gin");
        entity.setDisponible(true);
        return entity;
    }

    private PedidoEntity pedido(EstadoPedido estado, CoctelEntity coctel, ModificadorEntity modificador) {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setNumeroMesa(7);
        pedido.setEstado(estado);
        pedido.setFechaCreacion(LocalDateTime.now());
        ItemPedidoEntity item = new ItemPedidoEntity();
        item.setPedido(pedido);
        item.setCoctel(coctel);
        item.setNombreCoctel(coctel.getNombre());
        item.setTipo(coctel.getTipo());
        item.setPrecioUnitario(coctel.getPrecio());
        item.setCantidad(1);
        item.setDestilado("Tanqueray");
        item.setCambiosDeLicor(0);
        if (modificador != null) {
            ItemModificadorEntity enlace = new ItemModificadorEntity();
            enlace.setItem(item);
            enlace.setModificador(modificador);
            enlace.setPrecioExtra(modificador.getPrecioExtra());
            item.getModificadores().add(enlace);
        }
        pedido.getItems().add(item);
        return pedido;
    }

    @Test
    void buscarConItemsTraeLosItemsYSusModificadores() {
        PedidoEntity encontrado = pedidoRepository.buscarConItems(recibido.getId()).orElseThrow();

        assertThat(encontrado.getItems()).hasSize(1);
        ItemPedidoEntity item = encontrado.getItems().get(0);
        assertThat(item.getCoctel().getId()).isEqualTo(negroni.getId());
        assertThat(item.getModificadores()).hasSize(1);
        assertThat(item.getModificadores().get(0).getPrecioExtra()).isEqualTo(9000.0);
    }

    @Test
    void comandasActivasExcluyenLasFinalizadas() {
        assertThat(pedidoRepository.findByEstadoNotInOrderByFechaCreacionAsc(
                EnumSet.of(EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO)))
                .extracting(PedidoEntity::getId).containsExactly(recibido.getId());
        assertThat(pedidoRepository.findByEstadoOrderByIdAsc(EstadoPedido.ENTREGADO)).hasSize(1);
        assertThat(pedidoRepository.findAllByOrderByIdAsc()).hasSize(2);
    }

    @Test
    void detectaSiUnCoctelTieneComandas() {
        assertThat(pedidoRepository.existsByItemsCoctelId(negroni.getId())).isTrue();
        assertThat(pedidoRepository.existsByItemsCoctelId(martini.getId())).isFalse();
    }

    @Test
    void borrarComandaEliminaSusItemsEnCascada() {
        pedidoRepository.deleteById(recibido.getId());
        entityManager.flush();

        assertThat(pedidoRepository.findById(recibido.getId())).isEmpty();
        assertThat(pedidoRepository.count()).isEqualTo(1);
    }
}
