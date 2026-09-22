package com.restaurante.integration;

import com.restaurante.RestauranteApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integracion: levanta Spring completo y recorre el flujo principal de Blue Velvet.
 */
@SpringBootTest(classes = RestauranteApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RestauranteApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void documentacionOpenApiDisponible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Blue Velvet API"));
    }

    @Test
    void flujoCompletoDeUnaComanda() throws Exception {
        mockMvc.perform(post("/api/v1/cocteles").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Martini Seco","precio":39000,"categoria":"CLASICO",
                                 "tipo":"ALCOHOLICA","destiladoBase":"Gin"}
                                """))
                .andExpect(status().isCreated());

        String idCoctel = "1";
        mockMvc.perform(post("/api/v1/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroMesa\":3,\"items\":[{\"idCoctel\":" + idCoctel + ",\"cantidad\":1}]}"))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(post("/api/v1/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroMesa\":3,\"items\":[{\"idCoctel\":" + idCoctel
                                + ",\"cantidad\":1,\"destilado\":\"Tanqueray\"}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("RECIBIDO"));

        mockMvc.perform(patch("/api/v1/pedidos/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"EN_PREPARACION\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PREPARACION"));

        mockMvc.perform(get("/api/v1/ruta-inexistente"))
                .andExpect(status().isNotFound());
    }
}
