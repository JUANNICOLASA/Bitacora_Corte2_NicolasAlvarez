package com.restaurante.persistence.document;

import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.TipoEvento;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "eventos_comanda")
@Getter
@Setter
@NoArgsConstructor
public class EventoComandaDocument {

    @Id
    private String id;

    @Indexed
    @Field("id_pedido")
    private Long idPedido;

    private TipoEvento tipo;

    private String descripcion;

    private EstadoPedido estado;

    private String usuario;

    @Indexed
    private LocalDateTime fecha;
}
