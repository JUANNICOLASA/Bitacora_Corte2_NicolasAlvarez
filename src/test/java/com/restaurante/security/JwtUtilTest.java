package com.restaurante.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "cHJ1ZWJhcy1ibHVlLXZlbHZldC1jbGF2ZS1kZS1maXJtYS1qd3QtMjU2LWJpdHM=";
    private static final String OTRO_SECRET = "b3RyYS1jbGF2ZS1kaXN0aW50YS1wYXJhLWZpcm1hci1sb3MtdG9rZW5zLWp3dA==";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET, 60000);

    @Test
    void generaTokenConCorreoYRol() {
        String token = jwtUtil.generarToken("admin@bluevelvet.com", "ADMIN");

        Claims claims = jwtUtil.extraerClaims(token);
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(claims.getSubject()).isEqualTo("admin@bluevelvet.com");
        assertThat(jwtUtil.extraerRol(claims)).isEqualTo("ADMIN");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
        assertThat(jwtUtil.esValido(token)).isTrue();
    }

    @Test
    void tokenExpiradoNoEsValido() {
        String token = new JwtUtil(SECRET, -1000).generarToken("admin@bluevelvet.com", "ADMIN");

        assertThat(jwtUtil.esValido(token)).isFalse();
    }

    @Test
    void tokenFirmadoConOtraClaveNoEsValido() {
        String token = new JwtUtil(OTRO_SECRET, 60000).generarToken("admin@bluevelvet.com", "ADMIN");

        assertThat(jwtUtil.esValido(token)).isFalse();
    }

    @Test
    void tokenMalFormadoOVacioNoEsValido() {
        assertThat(jwtUtil.esValido("tokenbasura")).isFalse();
        assertThat(jwtUtil.esValido("")).isFalse();
    }
}
