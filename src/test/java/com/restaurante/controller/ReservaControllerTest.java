package com.restaurante.controller;

import com.restaurante.exception.ReglaNegocioException;
import com.restaurante.mapper.ReservaMapper;
import com.restaurante.model.domain.EstadoReserva;
import com.restaurante.model.domain.Reserva;
import com.restaurante.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReservaControllerTest {

    private static final String URL = "/api/v1/reservas";
    private static final String BODY = """
            {"nombre":"Laura Gomez","telefono":"3005550186","correo":"laura@correo.com","fecha":"2026-12-04",
             "hora":"20:00","personas":2,"ocasion":"Aniversario"}
            """;

    @Mock
    private ReservaService reservaService;

    private MockMvc mockMvc;

    private final UUID id = UUID.randomUUID();

    private final Reserva reserva = Reserva.builder().id(id).codigo("BV-ABC234").nombre("Laura Gomez")
            .correo("laura@correo.com").fecha(LocalDate.of(2026, 12, 4)).hora(LocalTime.of(20, 0)).personas(2)
            .estado(EstadoReserva.CONFIRMADA).build();

    @BeforeEach
    void setUp() {
        ReservaController controller = new ReservaController(reservaService, Mappers.getMapper(ReservaMapper.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void crearDevuelve201ConCodigoYHoraCorta() throws Exception {
        when(reservaService.crear(any(Reserva.class), isNull())).thenReturn(reserva);

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("BV-ABC234"))
                .andExpect(jsonPath("$.hora").value("20:00"));
    }

    @Test
    void crearConDatosInvalidosDevuelve400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nombre":"La","telefono":"12","correo":"no-es-correo","fecha":"2026-12-04",
                         "hora":"20:00","personas":20}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.length()").value(4));
    }

    @Test
    void crearEnLunesDevuelve422() throws Exception {
        when(reservaService.crear(any(Reserva.class), isNull()))
                .thenThrow(new ReglaNegocioException("Los lunes la casa descansa. Elija otro dia"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void agendaYMisReservas() throws Exception {
        when(reservaService.listar(LocalDate.of(2026, 12, 4))).thenReturn(List.of(reserva));
        when(reservaService.listarMias(isNull())).thenReturn(List.of());

        mockMvc.perform(get(URL).param("fecha", "2026-12-04"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Laura Gomez"));
        mockMvc.perform(get(URL + "/mias")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void cambiarEstadoYCancelar() throws Exception {
        reserva.setEstado(EstadoReserva.CUMPLIDA);
        when(reservaService.cambiarEstado(id, EstadoReserva.CUMPLIDA)).thenReturn(reserva);

        mockMvc.perform(patch(URL + "/" + id + "/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"CUMPLIDA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CUMPLIDA"));
        mockMvc.perform(delete(URL + "/" + id)).andExpect(status().isNoContent());
        verify(reservaService).cancelar(id, null);
    }

    @Test
    void idInvalidoDevuelve400() throws Exception {
        mockMvc.perform(delete(URL + "/no-es-un-uuid")).andExpect(status().isBadRequest());
    }
}
