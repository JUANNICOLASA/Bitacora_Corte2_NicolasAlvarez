package com.restaurante.security;

import com.restaurante.persistence.entity.UsuarioEntity;
import com.restaurante.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// S10 - UserDetailsService: Spring Security lo usa en el login para cargar el usuario de la BD
// y comparar la contrasena enviada contra el hash BCrypt guardado.
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        UsuarioEntity usuario = usuarioRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .build();
    }
}
