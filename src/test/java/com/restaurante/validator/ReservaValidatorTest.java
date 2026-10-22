package com.restaurante.validator;

import com.restaurante.exception.ReglaNegocioException;
import com.restaurante.model.domain.EstadoReserva;
import com.restaurante.model.domain.Reserva;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservaValidatorTest {

    private final ReservaValidator validator = new ReservaValidator(
            Clock.fixed(Instant.parse("2026-10-22T03:00:00Z"), ZoneId.of("America/Bogota")));

    private Authentication usuario(String correo, String rol) {
        return new UsernamePasswordAuthenticationToken(correo, null, List.of(new SimpleGrantedAuthority(rol)));
    }

    @Test
    void aceptaHoyEnBogotaYDiasProximos() {
        assertThatCode(() -> validator.validarFecha(LocalDate.of(2026, 10, 21))).doesNotThrowAnyException();
        assertThatCode(() -> validator.validarFecha(LocalDate.of(2026, 10, 24))).doesNotThrowAnyException();
    }

    @Test
    void rechazaFechasPasadas() {
        assertThatThrownBy(() -> validator.validarFecha(LocalDate.of(2026, 10, 20)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya paso");
    }

    @Test
    void rechazaLosLunes() {
        assertThatThrownBy(() -> validator.validarFecha(LocalDate.of(2026, 10, 26)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("lunes");
    }

    @ParameterizedTest
    @ValueSource(strings = {"18:00", "20:30", "23:30", "00:00"})
    void aceptaHorasDelHorario(String hora) {
        assertThatCode(() -> validator.validarHora(LocalTime.parse(hora))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"17:30", "20:15", "00:30", "12:00", "19:00:30"})
    void rechazaHorasFueraDelHorario(String hora) {
        assertThatThrownBy(() -> validator.validarHora(LocalTime.parse(hora)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void soloPasaDeConfirmadaACumplidaOCancelada() {
        assertThatCode(() -> validator.validarCambioEstado(EstadoReserva.CONFIRMADA, EstadoReserva.CUMPLIDA))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validarCambioEstado(EstadoReserva.CONFIRMADA, EstadoReserva.CANCELADA))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> validator.validarCambioEstado(EstadoReserva.CONFIRMADA, EstadoReserva.CONFIRMADA))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> validator.validarCambioEstado(EstadoReserva.CANCELADA, EstadoReserva.CUMPLIDA))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void elClienteSoloManejaSusReservas() {
        Reserva reserva = Reserva.builder().codigo("BV-ABC234").usuario("laura@correo.com").build();

        assertThatCode(() -> validator.validarPropietario(reserva, usuario("laura@correo.com", "ROLE_CLIENTE")))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validarPropietario(reserva, usuario("mesero@bluevelvet.com", "ROLE_MESERO")))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> validator.validarPropietario(reserva, usuario("otro@correo.com", "ROLE_CLIENTE")))
                .isInstanceOf(AccessDeniedException.class);
    }
}
