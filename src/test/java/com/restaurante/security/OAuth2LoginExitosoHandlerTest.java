package com.restaurante.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OAuth2LoginExitosoHandlerTest {

    @Test
    void devuelveUnJwtPropioConLosDatosDeGoogle() throws Exception {
        AuthService authService = mock(AuthService.class);
        when(authService.loginOAuth2("laura@gmail.com", "Laura Gomez")).thenReturn("jwt-generado");
        OAuth2LoginExitosoHandler handler = new OAuth2LoginExitosoHandler(authService, new ObjectMapper());
        DefaultOAuth2User usuarioGoogle = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("OAUTH2_USER")),
                Map.of("email", "laura@gmail.com", "name", "Laura Gomez"), "email");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response,
                new OAuth2AuthenticationToken(usuarioGoogle, usuarioGoogle.getAuthorities(), "google"));

        assertThat(response.getContentAsString()).contains("\"token\":\"jwt-generado\"", "\"tipo\":\"Bearer\"");
    }
}
