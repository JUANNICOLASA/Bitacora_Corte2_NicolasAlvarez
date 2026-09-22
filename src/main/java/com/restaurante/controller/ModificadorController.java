package com.restaurante.controller;

import com.restaurante.mapper.ModificadorMapper;
import com.restaurante.model.domain.Modificador;
import com.restaurante.model.dto.request.DisponibilidadRequestDTO;
import com.restaurante.model.dto.request.ModificadorRequestDTO;
import com.restaurante.model.dto.response.ErrorResponseDTO;
import com.restaurante.model.dto.response.ModificadorResponseDTO;
import com.restaurante.service.ModificadorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Administracion de los modificadores (adiciones) del inventario de barra.
 */
@Tag(name = "Modificadores", description = "Adiciones para personalizar los cocteles")
@RestController
@RequestMapping("/api/v1/modificadores")
@RequiredArgsConstructor
public class ModificadorController {

    private final ModificadorService modificadorService;
    private final ModificadorMapper modificadorMapper;

    @Operation(summary = "Listar modificadores")
    @ApiResponse(responseCode = "200", description = "Lista de modificadores")
    @GetMapping
    public ResponseEntity<List<ModificadorResponseDTO>> listar() {
        return ResponseEntity.ok(modificadorMapper.toResponseList(modificadorService.listar()));
    }

    @Operation(summary = "Obtener un modificador por id")
    @ApiResponse(responseCode = "200", description = "Modificador encontrado")
    @ApiResponse(responseCode = "404", description = "El modificador no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @GetMapping("/{id}")
    public ResponseEntity<ModificadorResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(modificadorMapper.toResponse(modificadorService.obtenerPorId(id)));
    }

    @Operation(summary = "Crear un modificador")
    @ApiResponse(responseCode = "201", description = "Modificador creado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "Nombre duplicado",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PostMapping
    public ResponseEntity<ModificadorResponseDTO> crear(@Valid @RequestBody ModificadorRequestDTO request) {
        Modificador creado = modificadorService.crear(modificadorMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(modificadorMapper.toResponse(creado));
    }

    @Operation(summary = "Marcar un modificador como disponible o agotado en barra")
    @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada")
    @ApiResponse(responseCode = "404", description = "El modificador no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<ModificadorResponseDTO> cambiarDisponibilidad(@PathVariable Long id,
                                                                        @Valid @RequestBody DisponibilidadRequestDTO request) {
        Modificador modificador = modificadorService.cambiarDisponibilidad(id, request.disponible());
        return ResponseEntity.ok(modificadorMapper.toResponse(modificador));
    }
}
