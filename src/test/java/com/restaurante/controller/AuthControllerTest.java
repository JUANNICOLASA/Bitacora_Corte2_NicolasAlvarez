package com.restaurante.controller;

import com.restaurante.exception.CredencialesInvalidasException;
import com.restaurante.mapper.UsuarioMapper;
import com.restaurante.model.domain.Rol;
import com.restaurante.model.domain.Usuario;
import com.restaurante.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final String URL = "/api/v1/auth";

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    private final Usuario cliente = Usuario.builder().id(3L).nombre("Laura").email("laura@correo.com")
            .password("$2a$10$hash").rol(Rol.CLIENTE).build();

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(authService, Mappers.getMapper(UsuarioMapper.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void loginDevuelveToken() throws Exception {
        when(authService.login("admin@bluevelvet.com", "Velvet2026*")).thenReturn("jwt");

        mockMvc.perform(post(URL + "/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@bluevelvet.com\",\"password\":\"Velvet2026*\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt"))
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    void loginIncorrectoDevuelve401() throws Exception {
        when(authService.login("admin@bluevelvet.com", "mala")).thenThrow(new CredencialesInvalidasException());

        mockMvc.perform(post(URL + "/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@bluevelvet.com\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("BV-401"));
    }

    @Test
    void loginConCorreoInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post(URL + "/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"no-es-correo\",\"password\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registroDevuelve201SinContrasena() throws Exception {
        when(authService.registrar(any(Usuario.class))).thenReturn(cliente);

        mockMvc.perform(post(URL + "/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Laura\",\"email\":\"laura@correo.com\",\"password\":\"Velvet2026*\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void meDevuelveElUsuarioAutenticado() throws Exception {
        when(authService.obtenerPorEmail("laura@correo.com")).thenReturn(cliente);

        mockMvc.perform(get(URL + "/me")
                        .principal(new UsernamePasswordAuthenticationToken("laura@correo.com", null, List.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("laura@correo.com"));
    }
}
