package com.restaurante.controller;

import com.restaurante.exception.RecursoDuplicadoException;
import com.restaurante.mapper.ResenaMapper;
import com.restaurante.model.domain.Resena;
import com.restaurante.model.domain.ResumenCalificacion;
import com.restaurante.model.domain.TipoProducto;
import com.restaurante.service.ResenaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ResenaControllerTest {

    private static final String URL = "/api/v1/resenas";

    @Mock
    private ResenaService resenaService;

    private MockMvc mockMvc;

    private final Resena resena = Resena.builder().id("abc").tipo(TipoProducto.COCTEL).idProducto(1L)
            .autor("Laura").usuario("laura@correo.com").estrellas(5).comentario("Excelente")
            .fecha(LocalDateTime.now()).build();

    @BeforeEach
    void setUp() {
        ResenaController controller = new ResenaController(resenaService, Mappers.getMapper(ResenaMapper.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listarNoExponeElCorreo() throws Exception {
        when(resenaService.listar(TipoProducto.COCTEL, 1L)).thenReturn(List.of(resena));

        mockMvc.perform(get(URL).param("tipo", "COCTEL").param("idProducto", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].autor").value("Laura"))
                .andExpect(jsonPath("$[0].usuario").doesNotExist());
    }

    @Test
    void listarSinParametrosDevuelve400() throws Exception {
        mockMvc.perform(get(URL).param("tipo", "COCTEL"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Falta el parametro idProducto"));
    }

    @Test
    void resumenPorProducto() throws Exception {
        when(resenaService.resumen()).thenReturn(List.of(ResumenCalificacion.builder().tipo(TipoProducto.PLATO)
                .idProducto(2L).promedio(4.5).total(2L).distribucion(Map.of(5, 1L, 4, 1L)).build()));

        mockMvc.perform(get(URL + "/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].promedio").value(4.5))
                .andExpect(jsonPath("$[0].distribucion.5").value(1));
    }

    @Test
    void crearDevuelve201() throws Exception {
        when(resenaService.crear(any(Resena.class), any())).thenReturn(resena);

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"COCTEL\",\"idProducto\":1,\"estrellas\":5,\"comentario\":\"Excelente\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estrellas").value(5));
    }

    @Test
    void crearConEstrellasFueraDeRangoDevuelve400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"COCTEL\",\"idProducto\":1,\"estrellas\":6}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void segundaCalificacionDevuelve409() throws Exception {
        when(resenaService.crear(any(Resena.class), any()))
                .thenThrow(new RecursoDuplicadoException("Ya calificaste este producto"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"PLATO\",\"idProducto\":2,\"estrellas\":3}"))
                .andExpect(status().isConflict());
    }
}
