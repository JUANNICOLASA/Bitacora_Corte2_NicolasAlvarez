package com.restaurante.persistence.document;

import com.restaurante.model.domain.TipoProducto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "resenas")
@CompoundIndex(name = "idx_resena_producto", def = "{'tipo': 1, 'id_producto': 1}")
@Getter
@Setter
@NoArgsConstructor
public class ResenaDocument {

    @Id
    private String id;

    private TipoProducto tipo;

    @Field("id_producto")
    private Long idProducto;

    private String autor;

    private String usuario;

    private Integer estrellas;

    private String comentario;

    @Indexed
    private LocalDateTime fecha;
}
