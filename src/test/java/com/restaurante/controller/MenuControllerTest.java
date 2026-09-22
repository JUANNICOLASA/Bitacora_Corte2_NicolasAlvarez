package com.restaurante.controller;

import com.restaurante.mapper.MenuMapper;
import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.Modificador;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.service.CoctelService;
import com.restaurante.service.ModificadorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MenuControllerTest {

    private static final String URL = "/api/v1/menu";

    @Mock
    private CoctelService coctelService;

    @Mock
    private ModificadorService modificadorService;

    private MockMvc mockMvc;

    private final Coctel pinaColada = Coctel.builder().id(5L).nombre("Pina Colada")
            .categoria(CategoriaCoctel.TROPICAL).tipo(TipoBebida.ALCOHOLICA).disponible(false).build();
    private final Coctel virginMojito = Coctel.builder().id(6L).nombre("Virgin Mojito")
            .categoria(CategoriaCoctel.SIN_ALCOHOL).tipo(TipoBebida.MOCKTAIL).disponible(true).build();

    @BeforeEach
    void setUp() {
        MenuController controller = new MenuController(coctelService, modificadorService,
                Mappers.getMapper(MenuMapper.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void cartaMuestraAgotadosConEtiqueta() throws Exception {
        when(coctelService.listar()).thenReturn(List.of(pinaColada));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].etiqueta").value("AGOTADO EN BARRA"))
                .andExpect(jsonPath("$[0].icono").value("candado"))
                .andExpect(jsonPath("$[0].seleccionable").value(false));
    }

    @Test
    void cartaFiltradaPorCategoria() throws Exception {
        when(coctelService.listarPorCategoria(CategoriaCoctel.SIN_ALCOHOL)).thenReturn(List.of(virginMojito));

        mockMvc.perform(get(URL).param("categoria", "SIN_ALCOHOL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Virgin Mojito"));
    }

    @Test
    void categoriaInvalidaDevuelve400() throws Exception {
        mockMvc.perform(get(URL).param("categoria", "CERVEZA"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void disponibles() throws Exception {
        when(coctelService.obtenerDisponibles()).thenReturn(List.of(virginMojito));

        mockMvc.perform(get(URL + "/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seleccionable").value(true));
    }

    @Test
    void detalle() throws Exception {
        when(coctelService.obtenerPorId(6L)).thenReturn(virginMojito);

        mockMvc.perform(get(URL + "/6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("MOCKTAIL"));
    }

    @Test
    void modificadoresConAlcoholBloqueadosEnMocktail() throws Exception {
        Modificador shot = Modificador.builder().id(1L).nombre("Shot extra")
                .graduacionAlcoholica(40.0).precioExtra(9000.0).disponible(true).build();
        when(coctelService.obtenerPorId(6L)).thenReturn(virginMojito);
        when(modificadorService.listar()).thenReturn(List.of(shot));

        mockMvc.perform(get(URL + "/6/modificadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].habilitado").value(false))
                .andExpect(jsonPath("$[0].etiqueta").value("NO DISPONIBLE PARA MOCKTAIL"));
    }
}
