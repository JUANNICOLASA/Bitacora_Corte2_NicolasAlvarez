package com.restaurante.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// S09 - Entidad intermedia en lugar de @ManyToMany entre item y modificador (recomendado en sistemas complejos).
// Ademas guarda el precio_extra congelado al momento del pedido.
@Entity
@Table(name = "item_modificador")
@Getter
@Setter
@NoArgsConstructor
public class ItemModificadorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @ManyToOne: muchas filas de item_modificador apuntan a un solo item. Fetch LAZY: carga solo cuando se usa
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_item", nullable = false)
    private ItemPedidoEntity item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_modificador", nullable = false)
    private ModificadorEntity modificador;

    @Column(name = "precio_extra", nullable = false)
    private Double precioExtra;
}
