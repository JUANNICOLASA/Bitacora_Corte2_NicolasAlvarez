package com.restaurante.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JwtAuthFilterTest {

    private final JwtUtil jwtUtil =
            new JwtUtil("cHJ1ZWJhcy1ibHVlLXZlbHZldC1jbGF2ZS1kZS1maXJtYS1qd3QtMjU2LWJpdHM=", 60000);
    private final JwtAuthFilter filtro = new JwtAuthFilter(jwtUtil);
    private final FilterChain chain = mock(FilterChain.class);

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest peticion(String authorization) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }
        return request;
    }

    @Test
    void tokenValidoAutenticaConElRolDelToken() throws Exception {
        MockHttpServletRequest request = peticion("Bearer " + jwtUtil.generarToken("bartender@bluevelvet.com", "BARTENDER"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, chain);

        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        assertThat(autenticacion.getName()).isEqualTo("bartender@bluevelvet.com");
        assertThat(autenticacion.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_BARTENDER");
        verify(chain).doFilter(request, response);
    }

    @Test
    void sinCabeceraNoAutentica() throws Exception {
        MockHttpServletRequest request = peticion(null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void cabeceraSinBearerNoAutentica() throws Exception {
        MockHttpServletRequest request = peticion("Basic dXN1YXJpbzpjbGF2ZQ==");

        filtro.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void tokenInvalidoNoAutentica() throws Exception {
        MockHttpServletRequest request = peticion("Bearer tokenbasura");

        filtro.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
