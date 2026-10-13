package com.restaurante.model.dto.response;

// S10 - Respuesta del login: {"token": "eyJ...", "tipo": "Bearer"}
public record TokenResponseDTO(
        String token,
        String tipo
) {
    public static TokenResponseDTO bearer(String token) {
        return new TokenResponseDTO(token, "Bearer");
    }
}
