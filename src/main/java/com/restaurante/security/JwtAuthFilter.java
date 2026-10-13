package com.restaurante.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// S10 - Filtro JWT, corre una vez por peticion (OncePerRequestFilter):
// 1. Lee el header Authorization: Bearer <token>
// 2. Si la firma es valida saca el email (subject) y el rol (claim) del token
// 3. Convierte el rol en GrantedAuthority "ROLE_X" para que funcione hasRole('X') en @PreAuthorize
// 4. Guarda la autenticacion en el SecurityContext. No se consulta la BD: autenticacion sin estado
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(PREFIJO)) {
            chain.doFilter(request, response);
            return;
        }
        String token = header.substring(PREFIJO.length());
        if (jwtUtil.esValido(token)) {
            Claims claims = jwtUtil.extraerClaims(token);
            List<SimpleGrantedAuthority> permisos =
                    List.of(new SimpleGrantedAuthority("ROLE_" + jwtUtil.extraerRol(claims)));
            UsernamePasswordAuthenticationToken autenticacion =
                    new UsernamePasswordAuthenticationToken(claims.getSubject(), null, permisos);
            autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(autenticacion);
        }
        chain.doFilter(request, response);
    }
}
