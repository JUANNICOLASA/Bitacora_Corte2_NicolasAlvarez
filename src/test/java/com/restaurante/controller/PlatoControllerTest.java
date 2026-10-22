package com.restaurante.controller;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.PlatoMapper;
import com.restaurante.model.domain.CategoriaPlato;
import com.restaurante.model.domain.Plato;
import com.restaurante.service.PlatoService;
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
import static org.mockito.ArgumentMatchers.eq;
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
class PlatoControllerTest {

    private static final String URL = "/api/v1/platos";
    private static final String BODY = """
            {"nombre":"Papas trufadas","precio":22000,"categoria":"PARA_COMPARTIR","porcion":"Para compartir"}
            """;

    @Mock
    private PlatoService platoService;

    private MockMvc mockMvc;

    private final Plato papas = Plato.builder().id(4L).nombre("Papas trufadas").precio(22000.0)
            .categoria(CategoriaPlato.PARA_COMPARTIR).porcion("Para compartir").disponible(true).build();

    @BeforeEach
    void setUp() {
        PlatoController controller = new PlatoController(platoService, Mappers.getMapper(PlatoMapper.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listarTodosYPorCategoria() throws Exception {
        when(platoService.listar()).thenReturn(List.of(papas));
        when(platoService.listarPorCategoria(CategoriaPlato.POSTRES)).thenReturn(List.of());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].porcion").value("Para compartir"));
        mockMvc.perform(get(URL).param("categoria", "POSTRES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        when(platoService.obtenerPorId(9L)).thenThrow(new RecursoNoEncontradoException("Plato", 9L));

        mockMvc.perform(get(URL + "/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("BV-404"));
    }

    @Test
    void obtenerExistente() throws Exception {
        when(platoService.obtenerPorId(4L)).thenReturn(papas);

        mockMvc.perform(get(URL + "/4")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(4));
    }

    @Test
    void crearDevuelve201YSinPrecioDevuelve400() throws Exception {
        when(platoService.crear(any(Plato.class))).thenReturn(papas);

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Papas trufadas"));
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Papas\",\"categoria\":\"POSTRES\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarCambiarDisponibilidadYEliminar() throws Exception {
        when(platoService.actualizar(eq(4L), any(Plato.class))).thenReturn(papas);
        when(platoService.cambiarDisponibilidad(4L, false)).thenReturn(papas);

        mockMvc.perform(put(URL + "/4").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());
        mockMvc.perform(patch(URL + "/4/disponibilidad").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"disponible\":false}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete(URL + "/4")).andExpect(status().isNoContent());
        verify(platoService).eliminar(4L);
    }
}
