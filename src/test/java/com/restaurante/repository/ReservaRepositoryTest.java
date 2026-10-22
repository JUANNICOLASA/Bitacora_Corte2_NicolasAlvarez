package com.restaurante.repository;

import com.restaurante.model.domain.EstadoReserva;
import com.restaurante.persistence.entity.ReservaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReservaRepositoryTest {

    private static final LocalDate DIA = LocalDate.of(2026, 12, 4);

    @Autowired
    private ReservaRepository reservaRepository;

    private ReservaEntity reserva(String codigo, LocalDate fecha, LocalTime hora, String usuario) {
        ReservaEntity entity = new ReservaEntity();
        entity.setCodigo(codigo);
        entity.setNombre("Laura Gomez");
        entity.setTelefono("3005550186");
        entity.setCorreo("laura@correo.com");
        entity.setFecha(fecha);
        entity.setHora(hora);
        entity.setPersonas(2);
        entity.setEstado(EstadoReserva.CONFIRMADA);
        entity.setUsuario(usuario);
        entity.setFechaCreacion(LocalDateTime.now());
        return entity;
    }

    @BeforeEach
    void setUp() {
        reservaRepository.save(reserva("BV-AAAAA2", DIA, LocalTime.of(22, 0), "laura@correo.com"));
        reservaRepository.save(reserva("BV-AAAAA3", DIA, LocalTime.of(19, 0), null));
        reservaRepository.save(reserva("BV-AAAAA4", DIA.minusDays(10), LocalTime.of(20, 0), "laura@correo.com"));
    }

    @Test
    void guardarAsignaUnUuid() {
        assertThat(reservaRepository.findAll()).allMatch(reserva -> reserva.getId() != null);
        assertThat(reservaRepository.existsByCodigo("BV-AAAAA2")).isTrue();
        assertThat(reservaRepository.existsByCodigo("BV-ZZZZZZ")).isFalse();
    }

    @Test
    void agendaDelDiaOrdenadaPorHora() {
        assertThat(reservaRepository.findByFechaOrderByHoraAsc(DIA))
                .extracting(ReservaEntity::getCodigo).containsExactly("BV-AAAAA3", "BV-AAAAA2");
        assertThat(reservaRepository.findByFechaGreaterThanEqualOrderByFechaAscHoraAsc(DIA)).hasSize(2);
    }

    @Test
    void reservasDeUnUsuarioDeLaMasRecienteALaMasAntigua() {
        assertThat(reservaRepository.findByUsuarioOrderByFechaDescHoraDesc("laura@correo.com"))
                .extracting(ReservaEntity::getCodigo).containsExactly("BV-AAAAA2", "BV-AAAAA4");
    }
}
