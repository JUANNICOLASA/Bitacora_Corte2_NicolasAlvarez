package com.restaurante.controller;

import com.restaurante.exception.BlueVelvetException;
import com.restaurante.model.dto.response.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

// S08 - Error Handler, "el gerente del restaurante": intercepta las excepciones de todos los controllers
// y responde siempre con el mismo formato (ErrorResponseDTO) y el codigo HTTP correcto.
// Asi no hay que llenar cada controller de try/catch.
// @RestControllerAdvice = @ControllerAdvice + @ResponseBody
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Excepciones propias de negocio: cada una ya trae su status (404, 409, 422, 429...) y su codigo interno
    @ExceptionHandler(BlueVelvetException.class)
    public ResponseEntity<ErrorResponseDTO> manejarNegocio(BlueVelvetException ex, HttpServletRequest request) {
        log.warn("Error de negocio {}: {}", ex.getCodigo(), ex.getMessage());
        return construir(ex.getStatus(), ex.getCodigo(), ex.getMessage(), request, List.of());
    }

    // Validaciones de input del DTO (@Valid) que fallaron -> 400 con el detalle de cada campo (armado con stream)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarValidacion(MethodArgumentNotValidException ex,
                                                              HttpServletRequest request) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .toList();
        log.warn("Validacion fallida en {}: {}", request.getRequestURI(), detalles);
        return construir(HttpStatus.BAD_REQUEST, "BV-400", "La peticion tiene datos invalidos", request, detalles);
    }

    // Body vacio o JSON mal formado -> 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> manejarCuerpoInvalido(HttpMessageNotReadableException ex,
                                                                  HttpServletRequest request) {
        log.warn("Cuerpo invalido en {}", request.getRequestURI());
        return construir(HttpStatus.BAD_REQUEST, "BV-400",
                "El cuerpo de la peticion esta vacio o mal formado", request, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> manejarTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                                HttpServletRequest request) {
        String mensaje = "El valor '" + ex.getValue() + "' no es valido para el parametro " + ex.getName();
        return construir(HttpStatus.BAD_REQUEST, "BV-400", mensaje, request, List.of());
    }

    // S09 - Se violo una restriccion de la BD (unique, llave foranea) -> 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> manejarIntegridad(DataIntegrityViolationException ex,
                                                              HttpServletRequest request) {
        log.warn("Conflicto de integridad en {}", request.getRequestURI());
        return construir(HttpStatus.CONFLICT, "BV-409",
                "La operacion entra en conflicto con datos existentes", request, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> manejarAccesoDenegado(AccessDeniedException ex,
                                                                  HttpServletRequest request) {
        log.warn("Acceso denegado a {} {}", request.getMethod(), request.getRequestURI());
        return construir(HttpStatus.FORBIDDEN, "BV-403",
                "No tiene permisos para realizar esta accion", request, List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> manejarRutaInexistente(NoResourceFoundException ex,
                                                                   HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, "BV-404", "La ruta solicitada no existe", request, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> manejarMetodoNoPermitido(HttpRequestMethodNotSupportedException ex,
                                                                     HttpServletRequest request) {
        return construir(HttpStatus.METHOD_NOT_ALLOWED, "BV-405",
                "El metodo " + ex.getMethod() + " no esta permitido en esta ruta", request, List.of());
    }

    // Handler generico al final: Spring evalua del mas especifico al mas general,
    // si este fuera el unico capturaria todo antes que los especificos.
    // S10 - OWASP Security Misconfiguration: al cliente no se le muestra el stacktrace,
    // solo un mensaje generico. El detalle queda en el log nivel ERROR.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> manejarGeneral(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en {}", request.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "BV-500",
                "Ocurrio un error inesperado. Intente de nuevo", request, List.of());
    }

    private ResponseEntity<ErrorResponseDTO> construir(HttpStatus status, String codigo, String mensaje,
                                                       HttpServletRequest request, List<String> detalles) {
        ErrorResponseDTO body = new ErrorResponseDTO(status.value(), codigo, mensaje,
                request.getRequestURI(), LocalDateTime.now(), detalles);
        return ResponseEntity.status(status).body(body);
    }
}
