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

// S09 - MongoDB (NoSQL de documentos, guarda BSON parecido a JSON). Se anota con @Document en vez de @Entity.
// Se uso Mongo para el historial de eventos: es un log de auditoria que solo se inserta y se consulta,
// no necesita relaciones ni joins. Persistencia HIBRIDA: PostgreSQL (relacional) + MongoDB (no relacional).
// Modelo REFERENCIADO: el evento guarda solo el idPedido (referencia al pedido de Postgres), no el pedido embebido.
// NoSQL sigue BASE (Basicamente disponible, Estado suave, Consistencia eventual) en vez de ACID.
@Document(collection = "eventos_comanda")
@Getter
@Setter
@NoArgsConstructor
public class EventoComandaDocument {

    // S09 - Id String: Mongo genera el ObjectId solo (trae timestamp, no fragmenta el indice). Ideal para muchos inserts
    @Id
    private String id;

    // S09 - Indice: se consulta por id_pedido, asi Mongo no recorre toda la coleccion. @Field cambia el nombre en BD
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
