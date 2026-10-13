package com.restaurante.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.model.dto.response.TokenResponseDTO;
import com.restaurante.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// S10 - OAuth 2: el usuario inicia sesion con Google, Google le da un authorization code a la app,
// Spring lo canjea por un access token y obtiene los datos del usuario.
// Aqui ese login se convierte en nuestro propio JWT. La app nunca conoce la contrasena de Google.
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginExitosoHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User usuario = (OAuth2User) authentication.getPrincipal();
        String email = usuario.getAttribute("email");
        String nombre = usuario.getAttribute("name");
        log.info("Login OAuth2 exitoso: {}", email);
        TokenResponseDTO token = TokenResponseDTO.bearer(authService.loginOAuth2(email, nombre));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), token);
    }
}
