package com.restaurante.controller;

import com.restaurante.exception.RecursoDuplicadoException;
import com.restaurante.exception.RecursoEnUsoException;
import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.CoctelMapper;
import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.service.CoctelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CoctelControllerTest {

    private static final String URL = "/api/v1/cocteles";
    private static final String BODY_VALIDO = """
            {"nombre":"Negroni","descripcion":"Gin, vermut y Campari","precio":38000,
             "categoria":"CLASICO","tipo":"ALCOHOLICA","destiladoBase":"Gin"}
            """;

    @Mock
    private CoctelService coctelService;

    private MockMvc mockMvc;

    private final Coctel negroni = Coctel.builder().id(1L).nombre("Negroni").precio(38000.0)
            .categoria(CategoriaCoctel.CLASICO).tipo(TipoBebida.ALCOHOLICA).destiladoBase("Gin")
            .disponible(true).build();

    @BeforeEach
    void setUp() {
        CoctelController controller = new CoctelController(coctelService, Mappers.getMapper(CoctelMapper.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listarDevuelve200() throws Exception {
        when(coctelService.listar()).thenReturn(List.of(negroni));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Negroni"));
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        when(coctelService.obtenerPorId(9L)).thenThrow(new RecursoNoEncontradoException("Coctel", 9L));

        mockMvc.perform(get(URL + "/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("BV-404"))
                .andExpect(jsonPath("$.ruta").value(URL + "/9"));
    }

    @Test
    void obtenerExistenteDevuelve200() throws Exception {
        when(coctelService.obtenerPorId(1L)).thenReturn(negroni);

        mockMvc.perform(get(URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destiladoBase").value("Gin"));
    }

    @Test
    void crearDevuelve201() throws Exception {
        when(coctelService.crear(any(Coctel.class))).thenReturn(negroni);

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void crearConBodyVacioDevuelve400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("BV-400"));
    }

    @Test
    void crearConCamposInvalidosDevuelve400ConDetalles() throws Exception {
        String body = """
                {"nombre":"","precio":-5,"categoria":"CLASICO","tipo":"ALCOHOLICA"}
                """;

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.length()").value(2));
    }

    @Test
    void crearDuplicadoDevuelve409() throws Exception {
        when(coctelService.crear(any(Coctel.class))).thenThrow(new RecursoDuplicadoException("duplicado"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizarDevuelve200() throws Exception {
        when(coctelService.actualizar(eq(1L), any(Coctel.class))).thenReturn(negroni);

        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON).content(BODY_VALIDO))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarDisponibilidadDevuelve200() throws Exception {
        when(coctelService.cambiarDisponibilidad(1L, false)).thenReturn(negroni);

        mockMvc.perform(patch(URL + "/1/disponibilidad").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"disponible\":false}"))
                .andExpect(status().isOk());
    }

    @Test
    void eliminarDevuelve204() throws Exception {
        mockMvc.perform(delete(URL + "/1"))
                .andExpect(status().isNoContent());
        verify(coctelService).eliminar(1L);
    }

    @Test
    void eliminarInexistenteDevuelve404() throws Exception {
        doThrow(new RecursoNoEncontradoException("Coctel", 2L)).when(coctelService).eliminar(2L);

        mockMvc.perform(delete(URL + "/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void idConFormatoInvalidoDevuelve400() throws Exception {
        mockMvc.perform(get(URL + "/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void errorInesperadoDevuelve500() throws Exception {
        when(coctelService.listar()).thenThrow(new IllegalStateException("fallo"));

        mockMvc.perform(get(URL))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("BV-500"));
    }

    @Test
    void metodoNoPermitidoDevuelve405() throws Exception {
        mockMvc.perform(put(URL).contentType(MediaType.APPLICATION_JSON).content(BODY_VALIDO))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void eliminarConComandasDevuelve409() throws Exception {
        doThrow(new RecursoEnUsoException("tiene comandas")).when(coctelService).eliminar(3L);

        mockMvc.perform(delete(URL + "/3"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("BV-409"));
    }

    @Test
    void conflictoDeIntegridadDevuelve409() throws Exception {
        when(coctelService.crear(any(Coctel.class))).thenThrow(new DataIntegrityViolationException("unique"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY_VALIDO))
                .andExpect(status().isConflict());
    }
}
