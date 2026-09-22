package com.restaurante.controller;

import com.restaurante.exception.ReglaNegocioException;
import com.restaurante.mapper.PedidoMapper;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.service.PedidoService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PedidoControllerTest {

    private static final String URL = "/api/v1/pedidos";

    @Mock
    private PedidoService pedidoService;

    private MockMvc mockMvc;

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        PedidoController controller = new PedidoController(pedidoService, Mappers.getMapper(PedidoMapper.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        ItemPedido item = ItemPedido.builder().id(1L).idCoctel(2L).nombreCoctel("Negroni")
                .tipo(TipoBebida.ALCOHOLICA).precioUnitario(38000.0).cantidad(1).destilado("Tanqueray").build();
        pedido = Pedido.builder().id(1L).numeroMesa(7).estado(EstadoPedido.RECIBIDO)
                .fechaCreacion(LocalDateTime.now()).items(List.of(item)).build();
    }

    @Test
    void crearDevuelve201() throws Exception {
        when(pedidoService.crear(any(Pedido.class))).thenReturn(pedido);

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroMesa\":7,\"items\":[{\"idCoctel\":2,\"cantidad\":1,\"destilado\":\"Tanqueray\"}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("RECIBIDO"))
                .andExpect(jsonPath("$.total").value(38000.0));
    }

    @Test
    void crearSinItemsDevuelve400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroMesa\":7,\"items\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearConItemInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroMesa\":7,\"items\":[{\"idCoctel\":2,\"cantidad\":0}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearConReglaVioladaDevuelve422() throws Exception {
        when(pedidoService.crear(any(Pedido.class)))
                .thenThrow(new ReglaNegocioException("requiere especificar la marca o tipo exacto de destilado"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroMesa\":7,\"items\":[{\"idCoctel\":2,\"cantidad\":1}]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("BV-422"));
    }

    @Test
    void listarTodosYPorEstado() throws Exception {
        when(pedidoService.listar()).thenReturn(List.of(pedido));
        when(pedidoService.listarPorEstado(EstadoPedido.RECIBIDO)).thenReturn(List.of(pedido));

        mockMvc.perform(get(URL)).andExpect(status().isOk());
        mockMvc.perform(get(URL).param("estado", "RECIBIDO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void listarActivos() throws Exception {
        when(pedidoService.listarActivos()).thenReturn(List.of(pedido));

        mockMvc.perform(get(URL + "/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void obtener() throws Exception {
        when(pedidoService.obtenerPorId(1L)).thenReturn(pedido);

        mockMvc.perform(get(URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].destilado").value("Tanqueray"));
    }

    @Test
    void cambiarEstado() throws Exception {
        when(pedidoService.cambiarEstado(1L, EstadoPedido.EN_PREPARACION)).thenReturn(pedido);

        mockMvc.perform(patch(URL + "/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"EN_PREPARACION\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarEstadoConValorInexistenteDevuelve400() throws Exception {
        mockMvc.perform(patch(URL + "/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"FLAMEADO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cambiarDestilado() throws Exception {
        when(pedidoService.cambiarDestilado(1L, 1L, "Hendrick's")).thenReturn(pedido);

        mockMvc.perform(patch(URL + "/1/items/1/destilado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"destilado\":\"Hendrick's\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void cancelarDevuelve204() throws Exception {
        mockMvc.perform(delete(URL + "/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void cancelarEnPreparacionDevuelve422() throws Exception {
        doThrow(new ReglaNegocioException("Solo se puede cancelar en RECIBIDO"))
                .when(pedidoService).cancelar(2L);

        mockMvc.perform(delete(URL + "/2"))
                .andExpect(status().isUnprocessableEntity());
    }
}
