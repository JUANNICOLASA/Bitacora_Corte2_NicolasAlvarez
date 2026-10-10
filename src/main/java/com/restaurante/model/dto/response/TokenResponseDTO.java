package com.restaurante.model.dto.response;

public record TokenResponseDTO(
        String token,
        String tipo
) {
    public static TokenResponseDTO bearer(String token) {
        return new TokenResponseDTO(token, "Bearer");
    }
}
