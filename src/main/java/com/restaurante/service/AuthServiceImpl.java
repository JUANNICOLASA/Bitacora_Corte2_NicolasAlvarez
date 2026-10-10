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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    static final int MAX_INTENTOS = 5;
    static final long MINUTOS_BLOQUEO = 15;

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioEntityMapper usuarioEntityMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private final Map<String, Intento> intentosFallidos = new ConcurrentHashMap<>();

    private record Intento(int cantidad, LocalDateTime ultimo) {
    }

    @Override
    @Transactional(readOnly = true)
    public String login(String email, String password) {
        String correo = normalizar(email);
        validarNoBloqueado(correo);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(correo, password));
        } catch (AuthenticationException ex) {
            intentosFallidos.merge(correo, new Intento(1, LocalDateTime.now()),
                    (anterior, nuevo) -> new Intento(anterior.cantidad() + 1, nuevo.ultimo()));
            log.warn("Login fallido para {}", correo);
            throw new CredencialesInvalidasException();
        }
        intentosFallidos.remove(correo);
        Usuario usuario = obtenerPorEmail(correo);
        log.info("Login exitoso: {} con rol {}", correo, usuario.getRol());
        return jwtUtil.generarToken(correo, usuario.getRol().name());
    }

    private void validarNoBloqueado(String email) {
        Intento intento = intentosFallidos.get(email);
        if (intento == null || intento.cantidad() < MAX_INTENTOS) {
            return;
        }
        if (intento.ultimo().plusMinutes(MINUTOS_BLOQUEO).isAfter(LocalDateTime.now())) {
            log.warn("Login bloqueado temporalmente para {}", email);
            throw new DemasiadosIntentosException(MINUTOS_BLOQUEO);
        }
        intentosFallidos.remove(email);
    }

    @Override
    public String loginOAuth2(String email, String nombre) {
        String correo = normalizar(email);
        Usuario usuario = usuarioRepository.findByEmail(correo)
                .map(usuarioEntityMapper::toDomain)
                .orElseGet(() -> crear(Usuario.builder()
                        .nombre(nombre == null ? correo : nombre)
                        .email(correo)
                        .password(UUID.randomUUID().toString())
                        .rol(Rol.CLIENTE)
                        .build()));
        return jwtUtil.generarToken(usuario.getEmail(), usuario.getRol().name());
    }

    @Override
    public Usuario registrar(Usuario usuario) {
        usuario.setRol(Rol.CLIENTE);
        return crear(usuario);
    }

    @Override
    public Usuario crear(Usuario usuario) {
        String correo = normalizar(usuario.getEmail());
        if (usuarioRepository.existsByEmail(correo)) {
            throw new RecursoDuplicadoException("Ya existe un usuario con el correo " + correo);
        }
        usuario.setId(null);
        usuario.setEmail(correo);
        usuario.setNombre(usuario.getNombre().trim());
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        UsuarioEntity guardado = usuarioRepository.save(usuarioEntityMapper.toEntity(usuario));
        log.info("Usuario creado: id={}, rol={}", guardado.getId(), guardado.getRol());
        return usuarioEntityMapper.toDomain(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existe(String email) {
        return usuarioRepository.existsByEmail(normalizar(email));
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(normalizar(email))
                .map(usuarioEntityMapper::toDomain)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", email));
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase();
    }
}
