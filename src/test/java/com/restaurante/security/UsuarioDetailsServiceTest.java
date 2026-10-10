package com.restaurante.security;

import com.restaurante.model.domain.Rol;
import com.restaurante.persistence.entity.UsuarioEntity;
import com.restaurante.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    void cargaElUsuarioConSuRol() {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setEmail("mesero@bluevelvet.com");
        entity.setPassword("$2a$10$hash");
        entity.setRol(Rol.MESERO);
        when(usuarioRepository.findByEmail("mesero@bluevelvet.com")).thenReturn(Optional.of(entity));

        UserDetails usuario = usuarioDetailsService.loadUserByUsername(" Mesero@BlueVelvet.com ");

        assertThat(usuario.getUsername()).isEqualTo("mesero@bluevelvet.com");
        assertThat(usuario.getPassword()).isEqualTo("$2a$10$hash");
        assertThat(usuario.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_MESERO");
    }

    @Test
    void usuarioInexistenteLanzaExcepcion() {
        when(usuarioRepository.findByEmail("nadie@bluevelvet.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioDetailsService.loadUserByUsername("nadie@bluevelvet.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
