package com.restaurante.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resena {

    private String id;
    private TipoProducto tipo;
    private Long idProducto;
    private String autor;
    private String usuario;
    private Integer estrellas;
    private String comentario;
    private LocalDateTime fecha;
}
