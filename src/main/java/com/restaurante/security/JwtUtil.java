package com.restaurante.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

// S10 - JWT (JSON Web Token) = Header.Payload.Signature
//  - Header: tipo de token y algoritmo de firma (HS256)
//  - Payload: claims NO sensibles (sub = email, rol, iat, exp). Nunca la contrasena, el payload es solo Base64
//  - Signature: firma HMAC con la clave secreta -> INTEGRIDAD: si alguien cambia el payload la firma ya no cuadra
// El secreto viene de la variable de entorno JWT_SECRET (Base64, minimo 256 bits), nunca del repositorio.
@Slf4j
@Component
public class JwtUtil {

    private static final String CLAIM_ROL = "rol";

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration-ms}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(String email, String rol) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_ROL, rol)
                .issuedAt(ahora)
                // TTL del token (jwt.expiration-ms, por defecto 1 hora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Token vencido, mal firmado o alterado -> false y la peticion queda sin autenticar (401)
    public boolean esValido(String token) {
        try {
            extraerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token JWT rechazado: {}", e.getMessage());
            return false;
        }
    }

    public String extraerRol(Claims claims) {
        return claims.get(CLAIM_ROL, String.class);
    }
}
