package com.restaurante.controller;

import com.restaurante.mapper.ReservaMapper;
import com.restaurante.model.domain.Reserva;
import com.restaurante.model.dto.request.CambioEstadoReservaRequestDTO;
import com.restaurante.model.dto.request.ReservaRequestDTO;
import com.restaurante.model.dto.response.ErrorResponseDTO;
import com.restaurante.model.dto.response.ReservaResponseDTO;
import com.restaurante.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Tag(name = "Reservas", description = "Reservas de mesa del bar")
@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;
    private final ReservaMapper reservaMapper;

    @Operation(summary = "Reservar una mesa",
            description = "Abierto al publico. Si el usuario inicio sesion la reserva queda asociada a su cuenta")
    @ApiResponse(responseCode = "201", description = "Reserva confirmada con su codigo")
    @ApiResponse(responseCode = "400", description = "Datos invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "422", description = "Fecha pasada, lunes u hora fuera del horario",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @SecurityRequirements
    @PostMapping
    public ResponseEntity<ReservaResponseDTO> crear(@Valid @RequestBody ReservaRequestDTO request,
                                                    Authentication usuario) {
        Reserva creada = reservaService.crear(reservaMapper.toDomain(request), usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaMapper.toResponse(creada));
    }

    @Operation(summary = "Agenda de reservas", description = "Sin fecha muestra las reservas desde hoy en adelante")
    @ApiResponse(responseCode = "200", description = "Reservas")
    @PreAuthorize("hasAnyRole('ADMIN','MESERO')")
    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> listar(
            @Parameter(description = "Dia a consultar (AAAA-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(reservaMapper.toResponseList(reservaService.listar(fecha)));
    }

    @Operation(summary = "Reservas del usuario autenticado")
    @ApiResponse(responseCode = "200", description = "Reservas hechas con la cuenta")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mias")
    public ResponseEntity<List<ReservaResponseDTO>> listarMias(Authentication usuario) {
        return ResponseEntity.ok(reservaMapper.toResponseList(reservaService.listarMias(usuario)));
    }

    @Operation(summary = "Marcar una reserva como cumplida o cancelada", description = "Solo desde CONFIRMADA")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "404", description = "La reserva no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "422", description = "Cambio de estado invalido",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PreAuthorize("hasAnyRole('ADMIN','MESERO')")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ReservaResponseDTO> cambiarEstado(@PathVariable UUID id,
                                                            @Valid @RequestBody CambioEstadoReservaRequestDTO request) {
        return ResponseEntity.ok(reservaMapper.toResponse(reservaService.cambiarEstado(id, request.estado())));
    }

    @Operation(summary = "Cancelar una reserva", description = "El cliente solo puede cancelar las suyas")
    @ApiResponse(responseCode = "204", description = "Reserva cancelada")
    @ApiResponse(responseCode = "403", description = "La reserva pertenece a otro cliente",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "La reserva no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "422", description = "La reserva ya no esta confirmada",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PreAuthorize("hasAnyRole('ADMIN','MESERO','CLIENTE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id, Authentication usuario) {
        reservaService.cancelar(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
