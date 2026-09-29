package com.restaurante.mapper;

import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Modificador;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.dto.request.ItemPedidoRequestDTO;
import com.restaurante.model.dto.request.PedidoRequestDTO;
import com.restaurante.model.dto.response.ItemPedidoResponseDTO;
import com.restaurante.model.dto.response.PedidoResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PedidoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    Pedido toDomain(PedidoRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nombreCoctel", ignore = true)
    @Mapping(target = "tipo", ignore = true)
    @Mapping(target = "precioUnitario", ignore = true)
    @Mapping(target = "cambiosDeLicor", ignore = true)
    @Mapping(target = "modificadores", source = "idsModificadores")
    ItemPedido toItemDomain(ItemPedidoRequestDTO dto);

    @Mapping(target = "total", expression = "java(pedido.calcularTotal())")
    PedidoResponseDTO toResponse(Pedido pedido);

    @Mapping(target = "subtotal", expression = "java(item.subtotal())")
    ItemPedidoResponseDTO toItemResponse(ItemPedido item);

    List<PedidoResponseDTO> toResponseList(List<Pedido> pedidos);

    default Modificador modificadorDesdeId(Long id) {
        return id == null ? null : Modificador.builder().id(id).build();
    }

    default String nombreModificador(Modificador modificador) {
        return modificador == null ? null : modificador.getNombre();
    }
}
