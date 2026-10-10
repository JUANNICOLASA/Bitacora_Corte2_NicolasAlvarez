package com.restaurante.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

class ManejadorErroresSeguridadTest {

    private final ManejadorErroresSeguridad manejador =
            new ManejadorErroresSeguridad(new ObjectMapper().registerModule(new JavaTimeModule()));

    @Test
    void sinAutenticacionResponde401ConFormatoUniforme() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/cocteles");
        MockHttpServletResponse response = new MockHttpServletResponse();

        manejador.commence(request, response, new InsufficientAuthenticationException("sin token"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).contains("\"codigo\":\"BV-401\"", "/api/v1/cocteles");
    }

    @Test
    void sinPermisosResponde403ConFormatoUniforme() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/v1/cocteles/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        manejador.handle(request, response, new AccessDeniedException("denegado"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("\"codigo\":\"BV-403\"");
    }
}
