package com.restaurante.mapper;

import com.restaurante.model.domain.Rol;
import com.restaurante.model.domain.Usuario;
import com.restaurante.model.dto.request.RegistroRequestDTO;
import com.restaurante.model.dto.response.UsuarioResponseDTO;
import com.restaurante.persistence.entity.UsuarioEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioMapperTest {

    private final UsuarioMapper mapper = Mappers.getMapper(UsuarioMapper.class);
    private final UsuarioEntityMapper entityMapper = Mappers.getMapper(UsuarioEntityMapper.class);

    @Test
    void registroADominioSinRol() {
        Usuario usuario = mapper.toDomain(new RegistroRequestDTO("Laura", "laura@correo.com", "Velvet2026*"));

        assertThat(usuario.getEmail()).isEqualTo("laura@correo.com");
        assertThat(usuario.getPassword()).isEqualTo("Velvet2026*");
        assertThat(usuario.getRol()).isNull();
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void dominioARespuestaSinContrasena() {
        Usuario usuario = Usuario.builder().id(1L).nombre("Laura").email("laura@correo.com")
                .password("$2a$10$hash").rol(Rol.CLIENTE).build();

        UsuarioResponseDTO respuesta = mapper.toResponse(usuario);

        assertThat(respuesta.rol()).isEqualTo(Rol.CLIENTE);
        assertThat(respuesta.email()).isEqualTo("laura@correo.com");
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    void dominioYEntidad() {
        Usuario usuario = Usuario.builder().id(1L).nombre("Admin").email("admin@bluevelvet.com")
                .password("$2a$10$hash").rol(Rol.ADMIN).build();

        UsuarioEntity entity = entityMapper.toEntity(usuario);
        Usuario deVuelta = entityMapper.toDomain(entity);

        assertThat(entity.getRol()).isEqualTo(Rol.ADMIN);
        assertThat(deVuelta).isEqualTo(usuario);
        assertThat(entityMapper.toEntity(null)).isNull();
        assertThat(entityMapper.toDomain(null)).isNull();
    }
}
