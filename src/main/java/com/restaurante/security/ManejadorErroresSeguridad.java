package com.restaurante.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.model.dto.response.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

// S10 - Los errores de seguridad pasan en los filtros, ANTES de llegar al controller, por eso el
// GlobalExceptionHandler no los alcanza a ver. Esta clase escribe el 401/403 con el mismo ErrorResponseDTO.
// commence -> 401 no autenticado (quien eres?) | handle -> 403 autenticado pero sin permiso (que puedes hacer?)
@Slf4j
@Component
@RequiredArgsConstructor
public class ManejadorErroresSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        log.warn("Acceso sin autenticacion a {} {}", request.getMethod(), request.getRequestURI());
        escribir(response, request, HttpStatus.UNAUTHORIZED, "BV-401",
                "Debe iniciar sesion con un token valido para usar este recurso");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        log.warn("Acceso denegado a {} {}", request.getMethod(), request.getRequestURI());
        escribir(response, request, HttpStatus.FORBIDDEN, "BV-403",
                "No tiene permisos para realizar esta accion");
    }

    private void escribir(HttpServletResponse response, HttpServletRequest request, HttpStatus status,
                          String codigo, String mensaje) throws IOException {
        ErrorResponseDTO body = new ErrorResponseDTO(status.value(), codigo, mensaje,
                request.getRequestURI(), LocalDateTime.now(), List.of());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
