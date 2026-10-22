package com.restaurante.service;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.exception.ReglaNegocioException;
import com.restaurante.mapper.ReservaEntityMapper;
import com.restaurante.model.domain.EstadoReserva;
import com.restaurante.model.domain.Reserva;
import com.restaurante.persistence.entity.ReservaEntity;
import com.restaurante.repository.ReservaRepository;
import com.restaurante.validator.ReservaValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 21);

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ReservaValidator reservaValidator;

    @Spy
    private ReservaEntityMapper reservaEntityMapper = Mappers.getMapper(ReservaEntityMapper.class);

    @Spy
    private Clock reloj = Clock.fixed(Instant.parse("2026-10-21T20:00:00Z"), ZoneId.of("America/Bogota"));

    @InjectMocks
    private ReservaServiceImpl reservaService;

    private final Authentication cliente = new UsernamePasswordAuthenticationToken("laura@correo.com", null,
            List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));

    private Reserva solicitud(String ocasion, String notas) {
        return Reserva.builder().nombre("  Laura Gomez ").telefono(" 3005550186 ").correo(" Laura@Correo.com ")
                .fecha(HOY.plusDays(2)).hora(LocalTime.of(20, 0)).personas(2).ocasion(ocasion).notas(notas).build();
    }

    private ReservaEntity guardada(EstadoReserva estado) {
        ReservaEntity entity = reservaEntityMapper.toEntity(solicitud("Cita", null));
        entity.setId(UUID.randomUUID());
        entity.setCodigo("BV-ABC234");
        entity.setEstado(estado);
        entity.setUsuario("laura@correo.com");
        return entity;
    }

    @Test
    void crearConfirmaLaReservaConCodigoYDatosLimpios() {
        when(reservaRepository.existsByCodigo(anyString())).thenReturn(true, false);
        when(reservaRepository.save(any(ReservaEntity.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Reserva creada = reservaService.crear(solicitud("  Aniversario ", "  "), cliente);

        assertThat(creada.getCodigo()).startsWith("BV-").hasSize(9);
        assertThat(creada.getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
        assertThat(creada.getNombre()).isEqualTo("Laura Gomez");
        assertThat(creada.getCorreo()).isEqualTo("laura@correo.com");
        assertThat(creada.getTelefono()).isEqualTo("3005550186");
        assertThat(creada.getOcasion()).isEqualTo("Aniversario");
        assertThat(creada.getNotas()).isNull();
        assertThat(creada.getUsuario()).isEqualTo("laura@correo.com");
        assertThat(creada.getFechaCreacion()).isNotNull();
    }

    @Test
    void crearSinSesionNoAsociaUsuario() {
        when(reservaRepository.save(any(ReservaEntity.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Reserva creada = reservaService.crear(solicitud(null, "Mesa tranquila"), null);

        assertThat(creada.getUsuario()).isNull();
        assertThat(creada.getOcasion()).isNull();
        assertThat(creada.getNotas()).isEqualTo("Mesa tranquila");
    }

    @Test
    void crearConFechaInvalidaNoGuarda() {
        doThrow(new ReglaNegocioException("Lunes")).when(reservaValidator).validarFecha(any());

        assertThatThrownBy(() -> reservaService.crear(solicitud(null, null), null))
                .isInstanceOf(ReglaNegocioException.class);
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void listarSinFechaMuestraDesdeHoy() {
        ReservaEntity reserva = guardada(EstadoReserva.CONFIRMADA);
        when(reservaRepository.findByFechaGreaterThanEqualOrderByFechaAscHoraAsc(HOY)).thenReturn(List.of(reserva));

        assertThat(reservaService.listar(null)).hasSize(1);
    }

    @Test
    void listarPorFechaYPorUsuario() {
        LocalDate fecha = HOY.plusDays(2);
        when(reservaRepository.findByFechaOrderByHoraAsc(fecha)).thenReturn(List.of());
        ReservaEntity reserva = guardada(EstadoReserva.CONFIRMADA);
        when(reservaRepository.findByUsuarioOrderByFechaDescHoraDesc("laura@correo.com")).thenReturn(List.of(reserva));

        assertThat(reservaService.listar(fecha)).isEmpty();
        assertThat(reservaService.listarMias(cliente)).extracting(Reserva::getCodigo).containsExactly("BV-ABC234");
    }

    @Test
    void cambiarEstadoValidaYGuarda() {
        ReservaEntity entity = guardada(EstadoReserva.CONFIRMADA);
        when(reservaRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(reservaRepository.save(entity)).thenReturn(entity);

        Reserva reserva = reservaService.cambiarEstado(entity.getId(), EstadoReserva.CUMPLIDA);

        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.CUMPLIDA);
        verify(reservaValidator).validarCambioEstado(EstadoReserva.CONFIRMADA, EstadoReserva.CUMPLIDA);
    }

    @Test
    void cancelarValidaElDuenoYCambiaElEstado() {
        ReservaEntity entity = guardada(EstadoReserva.CONFIRMADA);
        when(reservaRepository.findById(entity.getId())).thenReturn(Optional.of(entity));

        reservaService.cancelar(entity.getId(), cliente);

        assertThat(entity.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
        verify(reservaValidator).validarPropietario(any(Reserva.class), any());
        verify(reservaRepository).save(entity);
    }

    @Test
    void reservaInexistenteLanzaNoEncontrado() {
        UUID id = UUID.randomUUID();
        when(reservaRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.cancelar(id, cliente))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
