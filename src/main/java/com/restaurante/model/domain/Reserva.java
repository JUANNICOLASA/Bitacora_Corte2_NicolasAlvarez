package com.restaurante.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    private UUID id;
    private String codigo;
    private String nombre;
    private String telefono;
    private String correo;
    private LocalDate fecha;
    private LocalTime hora;
    private Integer personas;
    private String ocasion;
    private String notas;
    private EstadoReserva estado;
    private String usuario;
    private LocalDateTime fechaCreacion;
}
