package com.restaurante.integration;

import com.jayway.jsonpath.JsonPath;
import com.restaurante.RestauranteApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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

    @Autowired
    private MockMvc mockMvc;

    private Integer crear(String url, String body) throws Exception {
        String respuesta = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
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
                """);
        Integer idModificador = crear("/api/v1/modificadores",
                "{\"nombre\":\"Twist de limon\",\"graduacionAlcoholica\":0,\"precioExtra\":1000}");

        mockMvc.perform(post("/api/v1/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroMesa\":3,\"items\":[{\"idCoctel\":" + idCoctel + ",\"cantidad\":1}]}"))
                .andExpect(status().isUnprocessableEntity());

        Integer idPedido = crear("/api/v1/pedidos", "{\"numeroMesa\":3,\"items\":[{\"idCoctel\":" + idCoctel
                + ",\"cantidad\":2,\"destilado\":\"Tanqueray\",\"idsModificadores\":[" + idModificador + "]}]}");

        String comanda = mockMvc.perform(get("/api/v1/pedidos/" + idPedido))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECIBIDO"))
                .andExpect(jsonPath("$.total").value(80000.0))
                .andExpect(jsonPath("$.items[0].modificadores[0]").value("Twist de limon"))
                .andReturn().getResponse().getContentAsString();
        Integer idItem = JsonPath.read(comanda, "$.items[0].id");

        mockMvc.perform(patch("/api/v1/pedidos/" + idPedido + "/items/" + idItem + "/destilado")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"destilado\":\"Bombay\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].cambiosDeLicor").value(1));

        mockMvc.perform(patch("/api/v1/pedidos/" + idPedido + "/items/" + idItem + "/destilado")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"destilado\":\"Hendrick's\"}"))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(patch("/api/v1/pedidos/" + idPedido + "/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"EN_PREPARACION\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PREPARACION"));

        mockMvc.perform(get("/api/v1/pedidos/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(idPedido));

        mockMvc.perform(delete("/api/v1/cocteles/" + idCoctel))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/ruta-inexistente"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crudDeUnCoctelEnBaseDeDatos() throws Exception {
        Integer id = crear("/api/v1/cocteles", """
                {"nombre":"Espresso Martini","precio":37000,"categoria":"DE_AUTOR",
                 "tipo":"ALCOHOLICA","destiladoBase":"Vodka"}
                """);

        mockMvc.perform(get("/api/v1/cocteles/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Espresso Martini"));

        mockMvc.perform(patch("/api/v1/cocteles/" + id + "/disponibilidad")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disponible\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(false));

        mockMvc.perform(delete("/api/v1/cocteles/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/cocteles/" + id))
                .andExpect(status().isNotFound());
    }
}
