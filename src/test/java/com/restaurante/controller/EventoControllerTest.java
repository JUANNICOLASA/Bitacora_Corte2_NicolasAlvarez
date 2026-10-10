package com.restaurante.controller;

import com.restaurante.mapper.EventoComandaMapper;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.EventoComanda;
import com.restaurante.model.domain.TipoEvento;
import com.restaurante.service.EventoComandaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EventoControllerTest {

    @Mock
    private EventoComandaService eventoComandaService;

    private MockMvc mockMvc;

    private final EventoComanda evento = EventoComanda.builder().id("abc123").idPedido(1L)
            .tipo(TipoEvento.CAMBIO_ESTADO).estado(EstadoPedido.EN_PREPARACION)
            .descripcion("Estado cambiado de RECIBIDO a EN_PREPARACION")
            .usuario("bartender@bluevelvet.com").fecha(LocalDateTime.now()).build();

    @BeforeEach
    void setUp() {
        EventoController controller = new EventoController(eventoComandaService,
                Mappers.getMapper(EventoComandaMapper.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void listarTodos() throws Exception {
        when(eventoComandaService.listar(null)).thenReturn(List.of(evento));

        mockMvc.perform(get("/api/v1/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("CAMBIO_ESTADO"))
                .andExpect(jsonPath("$[0].usuario").value("bartender@bluevelvet.com"));
    }

    @Test
    void listarPorComanda() throws Exception {
        when(eventoComandaService.listar(1L)).thenReturn(List.of(evento));

        mockMvc.perform(get("/api/v1/eventos").param("idPedido", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPedido").value(1));
    }
}
