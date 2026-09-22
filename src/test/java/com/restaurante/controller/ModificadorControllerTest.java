package com.restaurante.controller;

import com.restaurante.mapper.ModificadorMapper;
import com.restaurante.model.domain.Modificador;
import com.restaurante.service.ModificadorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ModificadorControllerTest {

    private static final String URL = "/api/v1/modificadores";

    @Mock
    private ModificadorService modificadorService;

    private MockMvc mockMvc;

    private final Modificador agave = Modificador.builder().id(3L).nombre("Jarabe de agave")
            .graduacionAlcoholica(0.0).precioExtra(2000.0).disponible(true).build();

    @BeforeEach
    void setUp() {
        ModificadorController controller = new ModificadorController(modificadorService,
                Mappers.getMapper(ModificadorMapper.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listar() throws Exception {
        when(modificadorService.listar()).thenReturn(List.of(agave));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Jarabe de agave"));
    }

    @Test
    void obtener() throws Exception {
        when(modificadorService.obtenerPorId(3L)).thenReturn(agave);

        mockMvc.perform(get(URL + "/3"))
                .andExpect(status().isOk());
    }

    @Test
    void crearDevuelve201() throws Exception {
        when(modificadorService.crear(any(Modificador.class))).thenReturn(agave);

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Jarabe de agave\",\"graduacionAlcoholica\":0,\"precioExtra\":2000}"))
                .andExpect(status().isCreated());
    }

    @Test
    void crearConGraduacionMayorA100Devuelve400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Absenta\",\"graduacionAlcoholica\":120,\"precioExtra\":2000}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cambiarDisponibilidad() throws Exception {
        when(modificadorService.cambiarDisponibilidad(3L, false)).thenReturn(agave);

        mockMvc.perform(patch(URL + "/3/disponibilidad").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"disponible\":false}"))
                .andExpect(status().isOk());
    }
}
