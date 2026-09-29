package com.restaurante.mapper;

import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Modificador;
import com.restaurante.model.domain.Pedido;
import com.restaurante.persistence.entity.CoctelEntity;
import com.restaurante.persistence.entity.ItemModificadorEntity;
import com.restaurante.persistence.entity.ItemPedidoEntity;
import com.restaurante.persistence.entity.ModificadorEntity;
import com.restaurante.persistence.entity.PedidoEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;

@Mapper(componentModel = "spring")
public interface PedidoEntityMapper {

    PedidoEntity toEntity(Pedido pedido);

    @Mapping(target = "pedido", ignore = true)
    @Mapping(target = "coctel", source = "idCoctel")
    ItemPedidoEntity toItemEntity(ItemPedido item);

    ModificadorEntity toModificadorEntity(Modificador modificador);

    Pedido toDomain(PedidoEntity entity);

    @Mapping(target = "idCoctel", source = "coctel.id")
    ItemPedido toItemDomain(ItemPedidoEntity entity);

    default CoctelEntity coctelDesdeId(Long id) {
        if (id == null) {
            return null;
        }
        CoctelEntity coctel = new CoctelEntity();
        coctel.setId(id);
        return coctel;
    }

    default ItemModificadorEntity toItemModificador(Modificador modificador) {
        if (modificador == null) {
            return null;
        }
        ItemModificadorEntity entity = new ItemModificadorEntity();
        entity.setModificador(toModificadorEntity(modificador));
        entity.setPrecioExtra(modificador.getPrecioExtra() == null ? 0.0 : modificador.getPrecioExtra());
        return entity;
    }

    default Modificador toModificadorDomain(ItemModificadorEntity entity) {
        if (entity == null) {
            return null;
        }
        ModificadorEntity modificador = entity.getModificador();
        return Modificador.builder()
                .id(modificador.getId())
                .nombre(modificador.getNombre())
                .graduacionAlcoholica(modificador.getGraduacionAlcoholica())
                .disponible(modificador.getDisponible())
                .precioExtra(entity.getPrecioExtra())
                .build();
    }

    @AfterMapping
    default void enlazarRelaciones(@MappingTarget PedidoEntity pedido) {
        if (pedido.getItems() == null) {
            pedido.setItems(new ArrayList<>());
        }
        for (ItemPedidoEntity item : pedido.getItems()) {
            item.setPedido(pedido);
            if (item.getModificadores() == null) {
                item.setModificadores(new ArrayList<>());
            }
            item.getModificadores().forEach(modificador -> modificador.setItem(item));
        }
    }
}
