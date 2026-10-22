package com.restaurante.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.restaurante.model.domain.EstadoReserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record ReservaResponseDTO(
        UUID id,
        String codigo,
        String nombre,
        String telefono,
        String correo,
        LocalDate fecha,
        @JsonFormat(pattern = "HH:mm") LocalTime hora,
        Integer personas,
        String ocasion,
        String notas,
        EstadoReserva estado,
        LocalDateTime fechaCreacion
) {
}
