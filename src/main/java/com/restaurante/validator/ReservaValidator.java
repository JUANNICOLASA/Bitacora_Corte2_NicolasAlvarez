package com.restaurante.validator;

import com.restaurante.exception.ReglaNegocioException;
import com.restaurante.model.domain.EstadoReserva;
import com.restaurante.model.domain.Reserva;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ReservaValidator {

    public static final LocalTime PRIMERA_HORA = LocalTime.of(18, 0);
    public static final LocalTime ULTIMA_HORA = LocalTime.of(23, 30);
    private static final String ROL_CLIENTE = "ROLE_CLIENTE";
    private static final Set<EstadoReserva> ESTADOS_DESTINO = Set.of(EstadoReserva.CUMPLIDA, EstadoReserva.CANCELADA);

    private final Clock reloj;

    public void validarFecha(LocalDate fecha) {
        if (fecha.isBefore(LocalDate.now(reloj))) {
            throw new ReglaNegocioException("La fecha de la reserva ya paso. Elija hoy o un dia proximo");
        }
        if (fecha.getDayOfWeek() == DayOfWeek.MONDAY) {
            throw new ReglaNegocioException("Los lunes la casa descansa. Elija otro dia");
        }
    }

    public void validarHora(LocalTime hora) {
        boolean enPunto = hora.getSecond() == 0 && hora.getNano() == 0 && hora.getMinute() % 30 == 0;
        boolean enHorario = hora.equals(LocalTime.MIDNIGHT)
                || (!hora.isBefore(PRIMERA_HORA) && !hora.isAfter(ULTIMA_HORA));
        if (!enPunto || !enHorario) {
            throw new ReglaNegocioException("Las reservas se reciben de 18:00 a 00:00 cada media hora");
        }
    }

    public void validarCambioEstado(EstadoReserva actual, EstadoReserva nuevo) {
        if (actual != EstadoReserva.CONFIRMADA || !ESTADOS_DESTINO.contains(nuevo)) {
            throw new ReglaNegocioException("La reserva no puede pasar de " + actual + " a " + nuevo);
        }
    }

    public void validarPropietario(Reserva reserva, Authentication usuario) {
        boolean esCliente = usuario.getAuthorities().stream()
                .anyMatch(permiso -> ROL_CLIENTE.equals(permiso.getAuthority()));
        if (esCliente && !usuario.getName().equals(reserva.getUsuario())) {
            throw new AccessDeniedException("La reserva " + reserva.getCodigo() + " no pertenece al cliente");
        }
    }
}
