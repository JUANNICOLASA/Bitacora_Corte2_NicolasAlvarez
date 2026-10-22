package com.restaurante.service;

import com.restaurante.model.domain.EstadoReserva;
import com.restaurante.model.domain.Reserva;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReservaService {

    Reserva crear(Reserva reserva, Authentication usuario);

    List<Reserva> listar(LocalDate fecha);

    List<Reserva> listarMias(Authentication usuario);

    Reserva cambiarEstado(UUID id, EstadoReserva nuevoEstado);

    void cancelar(UUID id, Authentication usuario);
}
