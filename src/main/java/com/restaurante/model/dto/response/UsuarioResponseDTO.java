package com.restaurante.model.dto.response;

import com.restaurante.model.domain.Rol;

public record UsuarioResponseDTO(
        Long id,
        String nombre,
        String email,
        Rol rol
) {
}
