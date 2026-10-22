package com.restaurante.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecetaExterna {

    private String nombre;
    private String vaso;
    private String instrucciones;
    private List<String> ingredientes;
    private String imagen;
    private String fuente;
}
