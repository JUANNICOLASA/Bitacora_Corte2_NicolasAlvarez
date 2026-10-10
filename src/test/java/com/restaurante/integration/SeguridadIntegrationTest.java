package com.restaurante.integration;

import com.jayway.jsonpath.JsonPath;
import com.restaurante.RestauranteApplication;
import com.restaurante.model.domain.Rol;
import com.restaurante.model.domain.Usuario;
import com.restaurante.repository.EventoComandaRepository;
import com.restaurante.repository.UsuarioRepository;
import com.restaurante.security.JwtUtil;
import com.restaurante.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RestauranteApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadIntegrationTest {

    private static final String PASSWORD = "Velvet2026*";
    private static final String COCTEL = """
            {"nombre":"Bandeja Sour","precio":28000,"categoria":"SOUR","tipo":"ALCOHOLICA","destiladoBase":"Pisco"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${jwt.secret}")
    private String secret;

    @MockitoBean
    private EventoComandaRepository eventoComandaRepository;

    @BeforeEach
    void crearUsuarios() {
        crearSiNoExiste("admin.seguridad@bluevelvet.com", Rol.ADMIN);
        crearSiNoExiste("cliente.seguridad@bluevelvet.com", Rol.CLIENTE);
    }

    private void crearSiNoExiste(String email, Rol rol) {
        if (!authService.existe(email)) {
            authService.crear(Usuario.builder().nombre(rol.name()).email(email).password(PASSWORD).rol(rol).build());
        }
    }

    private String login(String email) throws Exception {
        String respuesta = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    @Test
    void loginExitosoDevuelveTokenYPermiteUsarlo() throws Exception {
        String token = login("admin.seguridad@bluevelvet.com");

        mockMvc.perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void loginConContrasenaIncorrectaDevuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin.seguridad@bluevelvet.com\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("BV-401"));
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/cocteles").contentType(MediaType.APPLICATION_JSON).content(COCTEL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("BV-401"));
    }

    @Test
    void tokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/pedidos").header(HttpHeaders.AUTHORIZATION, "Bearer tokenbasura"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenExpiradoDevuelve401() throws Exception {
        String expirado = new JwtUtil(secret, -1000).generarToken("admin.seguridad@bluevelvet.com", "ADMIN");

        mockMvc.perform(get("/api/v1/pedidos").header(HttpHeaders.AUTHORIZATION, "Bearer " + expirado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoPuedeCrearCoctelesDevuelve403() throws Exception {
        String token = login("cliente.seguridad@bluevelvet.com");

        mockMvc.perform(post("/api/v1/cocteles").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(COCTEL))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("BV-403"));
    }

    @Test
    void adminPuedeCrearCoctelesDevuelve201() throws Exception {
        String token = login("admin.seguridad@bluevelvet.com");

        mockMvc.perform(post("/api/v1/cocteles").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(COCTEL))
                .andExpect(status().isCreated());
    }

    @Test
    void httpBasicAutenticaConUsuarioYContrasena() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me").with(httpBasic("admin.seguridad@bluevelvet.com", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin.seguridad@bluevelvet.com"));
    }

    @Test
    void cartaEsPublicaYTieneCabecerasDeSeguridad() throws Exception {
        mockMvc.perform(get("/api/v1/menu"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-XSS-Protection", "1; mode=block"))
                .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    void registroGuardaLaContrasenaCifradaConRolCliente() throws Exception {
        mockMvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Laura\",\"email\":\"Laura@Correo.com\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.password").doesNotExist());

        String guardada = usuarioRepository.findByEmail("laura@correo.com").orElseThrow().getPassword();
        assertThat(guardada).startsWith("$2a$10$").isNotEqualTo(PASSWORD);
    }

    @Test
    void registroConContrasenaDebilDevuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Pedro\",\"email\":\"pedro@correo.com\",\"password\":\"12345678\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "otro.cliente@correo.com", roles = "CLIENTE")
    void clienteNoPuedeListarTodasLasComandas() throws Exception {
        mockMvc.perform(get("/api/v1/pedidos"))
                .andExpect(status().isForbidden());
    }

    @Test
    void corsPermiteElOrigenConfigurado() throws Exception {
        mockMvc.perform(options("/api/v1/menu")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"));

        mockMvc.perform(options("/api/v1/menu")
                        .header(HttpHeaders.ORIGIN, "http://sitio-malicioso.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }
}
