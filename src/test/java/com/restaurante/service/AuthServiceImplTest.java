package com.restaurante.service;

import com.restaurante.exception.CredencialesInvalidasException;
import com.restaurante.exception.DemasiadosIntentosException;
import com.restaurante.exception.RecursoDuplicadoException;
import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.UsuarioEntityMapper;
import com.restaurante.model.domain.Rol;
import com.restaurante.model.domain.Usuario;
import com.restaurante.persistence.entity.UsuarioEntity;
import com.restaurante.repository.UsuarioRepository;
import com.restaurante.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Spy
    private UsuarioEntityMapper usuarioEntityMapper = Mappers.getMapper(UsuarioEntityMapper.class);

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private UsuarioEntity entidad(String email, Rol rol) {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(1L);
        entity.setNombre("Usuario");
        entity.setEmail(email);
        entity.setPassword("$2a$10$hash");
        entity.setRol(rol);
        return entity;
    }

    private void simularGuardado() {
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenAnswer(invocacion -> {
            UsuarioEntity entity = invocacion.getArgument(0);
            entity.setId(5L);
            return entity;
        });
    }

    @Test
    void loginCorrectoDevuelveToken() {
        when(usuarioRepository.findByEmail("admin@bluevelvet.com"))
                .thenReturn(Optional.of(entidad("admin@bluevelvet.com", Rol.ADMIN)));
        when(jwtUtil.generarToken("admin@bluevelvet.com", "ADMIN")).thenReturn("token");

        String token = authService.login(" Admin@BlueVelvet.com ", "Velvet2026*");

        assertThat(token).isEqualTo("token");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void loginIncorrectoLanzaCredencialesInvalidas() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("mal"));

        assertThatThrownBy(() -> authService.login("admin@bluevelvet.com", "incorrecta"))
                .isInstanceOf(CredencialesInvalidasException.class);
        verify(jwtUtil, never()).generarToken(any(), any());
    }

    @Test
    void despuesDeCincoIntentosFallidosSeBloqueaElLogin() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("mal"));
        for (int i = 0; i < AuthServiceImpl.MAX_INTENTOS; i++) {
            assertThatThrownBy(() -> authService.login("mesero@bluevelvet.com", "incorrecta"))
                    .isInstanceOf(CredencialesInvalidasException.class);
        }

        assertThatThrownBy(() -> authService.login("mesero@bluevelvet.com", "Velvet2026*"))
                .isInstanceOf(DemasiadosIntentosException.class);
        verify(authenticationManager, times(AuthServiceImpl.MAX_INTENTOS)).authenticate(any());
    }

    @Test
    void registrarCreaUnClienteConContrasenaCifrada() {
        when(passwordEncoder.encode("Velvet2026*")).thenReturn("$2a$10$cifrada");
        simularGuardado();
        Usuario nuevo = Usuario.builder().nombre(" Laura ").email(" Laura@Correo.com ")
                .password("Velvet2026*").rol(Rol.ADMIN).build();

        Usuario creado = authService.registrar(nuevo);

        assertThat(creado.getId()).isEqualTo(5L);
        assertThat(creado.getRol()).isEqualTo(Rol.CLIENTE);
        assertThat(creado.getEmail()).isEqualTo("laura@correo.com");
        assertThat(creado.getNombre()).isEqualTo("Laura");
        assertThat(creado.getPassword()).isEqualTo("$2a$10$cifrada");
    }

    @Test
    void crearConCorreoRepetidoLanzaDuplicado() {
        when(usuarioRepository.existsByEmail("admin@bluevelvet.com")).thenReturn(true);
        Usuario repetido = Usuario.builder().nombre("Admin").email("admin@bluevelvet.com")
                .password("Velvet2026*").rol(Rol.ADMIN).build();

        assertThatThrownBy(() -> authService.crear(repetido))
                .isInstanceOf(RecursoDuplicadoException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void loginOAuth2DeUsuarioNuevoLoCreaComoCliente() {
        when(usuarioRepository.findByEmail("laura@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$aleatoria");
        simularGuardado();
        when(jwtUtil.generarToken("laura@gmail.com", "CLIENTE")).thenReturn("token");

        assertThat(authService.loginOAuth2("Laura@Gmail.com", "Laura Gomez")).isEqualTo("token");
        verify(usuarioRepository).save(any(UsuarioEntity.class));
    }

    @Test
    void loginOAuth2DeUsuarioExistenteConservaSuRol() {
        when(usuarioRepository.findByEmail("bartender@bluevelvet.com"))
                .thenReturn(Optional.of(entidad("bartender@bluevelvet.com", Rol.BARTENDER)));
        when(jwtUtil.generarToken("bartender@bluevelvet.com", "BARTENDER")).thenReturn("token");

        assertThat(authService.loginOAuth2("bartender@bluevelvet.com", null)).isEqualTo("token");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void loginOAuth2SinNombreUsaElCorreo() {
        when(usuarioRepository.findByEmail("sin.nombre@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$aleatoria");
        simularGuardado();
        when(jwtUtil.generarToken("sin.nombre@gmail.com", "CLIENTE")).thenReturn("token");

        assertThat(authService.loginOAuth2("sin.nombre@gmail.com", null)).isEqualTo("token");
    }

    @Test
    void existeYObtenerPorEmail() {
        when(usuarioRepository.existsByEmail("admin@bluevelvet.com")).thenReturn(true);
        when(usuarioRepository.findByEmail("admin@bluevelvet.com"))
                .thenReturn(Optional.of(entidad("admin@bluevelvet.com", Rol.ADMIN)));

        assertThat(authService.existe("ADMIN@bluevelvet.com")).isTrue();
        assertThat(authService.obtenerPorEmail("admin@bluevelvet.com").getRol()).isEqualTo(Rol.ADMIN);
    }

    @Test
    void obtenerPorEmailInexistenteLanzaNoEncontrado() {
        when(usuarioRepository.findByEmail("nadie@bluevelvet.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.obtenerPorEmail("nadie@bluevelvet.com"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
