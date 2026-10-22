package com.restaurante.integration;

import com.jayway.jsonpath.JsonPath;
import com.restaurante.RestauranteApplication;
import com.restaurante.repository.EventoComandaRepository;
import com.restaurante.repository.ResenaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RestauranteApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RestauranteApplicationTests {

    private static final RequestPostProcessor ADMIN = user("admin@bluevelvet.com").roles("ADMIN");
    private static final RequestPostProcessor BARTENDER = user("bartender@bluevelvet.com").roles("BARTENDER");
    private static final RequestPostProcessor MESERO = user("mesero@bluevelvet.com").roles("MESERO");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventoComandaRepository eventoComandaRepository;

    @MockitoBean
    private ResenaRepository resenaRepository;

    private Integer crear(String url, String body, RequestPostProcessor usuario) throws Exception {
        String respuesta = mockMvc.perform(post(url).with(usuario).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.id");
    }

    @Test
    void documentacionOpenApiDisponible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Blue Velvet API"));
    }

    @Test
    void flujoCompletoDeUnaComandaPersistida() throws Exception {
        Integer idCoctel = crear("/api/v1/cocteles", """
                {"nombre":"Martini Seco","precio":39000,"categoria":"CLASICO",
                 "tipo":"ALCOHOLICA","destiladoBase":"Gin"}
                """, ADMIN);
        Integer idModificador = crear("/api/v1/modificadores",
                "{\"nombre\":\"Twist de limon\",\"graduacionAlcoholica\":0,\"precioExtra\":1000}", ADMIN);

        mockMvc.perform(post("/api/v1/pedidos").with(MESERO).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroMesa\":3,\"items\":[{\"idCoctel\":" + idCoctel + ",\"cantidad\":1}]}"))
                .andExpect(status().isUnprocessableEntity());

        Integer idPedido = crear("/api/v1/pedidos", "{\"numeroMesa\":3,\"items\":[{\"idCoctel\":" + idCoctel
                + ",\"cantidad\":2,\"destilado\":\"Tanqueray\",\"idsModificadores\":[" + idModificador + "]}]}", MESERO);

        String comanda = mockMvc.perform(get("/api/v1/pedidos/" + idPedido).with(MESERO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECIBIDO"))
                .andExpect(jsonPath("$.total").value(80000.0))
                .andExpect(jsonPath("$.items[0].modificadores[0]").value("Twist de limon"))
                .andReturn().getResponse().getContentAsString();
        Integer idItem = JsonPath.read(comanda, "$.items[0].id");

        mockMvc.perform(patch("/api/v1/pedidos/" + idPedido + "/items/" + idItem + "/destilado").with(MESERO)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"destilado\":\"Bombay\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].cambiosDeLicor").value(1));

        mockMvc.perform(patch("/api/v1/pedidos/" + idPedido + "/items/" + idItem + "/destilado").with(MESERO)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"destilado\":\"Hendrick's\"}"))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(patch("/api/v1/pedidos/" + idPedido + "/estado").with(BARTENDER).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"EN_PREPARACION\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PREPARACION"));

        mockMvc.perform(get("/api/v1/pedidos/activos").with(BARTENDER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(idPedido));

        mockMvc.perform(delete("/api/v1/cocteles/" + idCoctel).with(ADMIN))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/ruta-inexistente").with(ADMIN))
                .andExpect(status().isNotFound());
    }

    @Test
    void crudDeUnCoctelEnBaseDeDatos() throws Exception {
        Integer id = crear("/api/v1/cocteles", """
                {"nombre":"Espresso Martini","precio":37000,"categoria":"DE_AUTOR",
                 "tipo":"ALCOHOLICA","destiladoBase":"Vodka"}
                """, ADMIN);

        mockMvc.perform(get("/api/v1/cocteles/" + id).with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Espresso Martini"));

        mockMvc.perform(patch("/api/v1/cocteles/" + id + "/disponibilidad").with(BARTENDER)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disponible\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(false));

        mockMvc.perform(delete("/api/v1/cocteles/" + id).with(ADMIN))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/cocteles/" + id).with(ADMIN))
                .andExpect(status().isNotFound());
    }

    @Test
    void reservaPublicaQueElMeseroVeEnLaAgenda() throws Exception {
        LocalDate fecha = LocalDate.now(ZoneId.of("America/Bogota")).plusDays(3);
        if (fecha.getDayOfWeek() == DayOfWeek.MONDAY) {
            fecha = fecha.plusDays(1);
        }
        String respuesta = mockMvc.perform(post("/api/v1/reservas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Laura Gomez\",\"telefono\":\"3005550186\",\"correo\":\"laura@correo.com\","
                                + "\"fecha\":\"" + fecha + "\",\"hora\":\"21:30\",\"personas\":4}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"))
                .andExpect(jsonPath("$.hora").value("21:30"))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(respuesta, "$.id");
        String codigo = JsonPath.read(respuesta, "$.codigo");

        mockMvc.perform(get("/api/v1/reservas").param("fecha", fecha.toString()).with(MESERO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == '" + codigo + "')].personas").value(4));

        mockMvc.perform(patch("/api/v1/reservas/" + id + "/estado").with(MESERO)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"CUMPLIDA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CUMPLIDA"));

        mockMvc.perform(delete("/api/v1/reservas/" + id).with(MESERO))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cocinaPublicaAdministradaPorElAdmin() throws Exception {
        String plato = "{\"nombre\":\"Tabla de quesos\",\"precio\":48000,\"categoria\":\"PARA_COMPARTIR\"}";
        Integer id = crear("/api/v1/platos", plato, ADMIN);

        mockMvc.perform(post("/api/v1/platos").with(MESERO).contentType(MediaType.APPLICATION_JSON).content(plato))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/platos/" + id + "/disponibilidad").with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disponible\":false}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/platos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(false));

        mockMvc.perform(delete("/api/v1/platos/" + id).with(ADMIN)).andExpect(status().isNoContent());
    }
}
