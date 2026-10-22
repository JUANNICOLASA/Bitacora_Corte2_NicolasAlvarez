package com.restaurante.controller;

import com.restaurante.mapper.PlatoMapper;
import com.restaurante.model.domain.CategoriaPlato;
import com.restaurante.model.domain.Plato;
import com.restaurante.model.dto.request.DisponibilidadRequestDTO;
import com.restaurante.model.dto.request.PlatoRequestDTO;
import com.restaurante.model.dto.response.ErrorResponseDTO;
import com.restaurante.model.dto.response.PlatoResponseDTO;
import com.restaurante.service.PlatoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Cocina", description = "Platos de la cocina que acompanan la carta")
@RestController
@RequestMapping("/api/v1/platos")
@RequiredArgsConstructor
public class PlatoController {

    private final PlatoService platoService;
    private final PlatoMapper platoMapper;

    @Operation(summary = "Listar los platos de la cocina", description = "Se puede filtrar por categoria")
    @ApiResponse(responseCode = "200", description = "Lista de platos")
    @ApiResponse(responseCode = "400", description = "Categoria invalida",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @SecurityRequirements
    @GetMapping
    public ResponseEntity<List<PlatoResponseDTO>> listar(
            @Parameter(description = "Filtrar por categoria")
            @RequestParam(required = false) CategoriaPlato categoria) {
        List<Plato> platos = categoria == null ? platoService.listar() : platoService.listarPorCategoria(categoria);
        return ResponseEntity.ok(platoMapper.toResponseList(platos));
    }

    @Operation(summary = "Obtener un plato por id")
    @ApiResponse(responseCode = "200", description = "Plato encontrado")
    @ApiResponse(responseCode = "404", description = "El plato no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @SecurityRequirements
    @GetMapping("/{id}")
    public ResponseEntity<PlatoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(platoMapper.toResponse(platoService.obtenerPorId(id)));
    }

    @Operation(summary = "Crear un plato")
    @ApiResponse(responseCode = "201", description = "Plato creado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "Nombre duplicado",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PlatoResponseDTO> crear(@Valid @RequestBody PlatoRequestDTO request) {
        Plato creado = platoService.crear(platoMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(platoMapper.toResponse(creado));
    }

    @Operation(summary = "Actualizar un plato completo")
    @ApiResponse(responseCode = "200", description = "Plato actualizado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "El plato no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "Nombre duplicado",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PlatoResponseDTO> actualizar(@PathVariable Long id,
                                                       @Valid @RequestBody PlatoRequestDTO request) {
        Plato actualizado = platoService.actualizar(id, platoMapper.toDomain(request));
        return ResponseEntity.ok(platoMapper.toResponse(actualizado));
    }

    @Operation(summary = "Marcar un plato como disponible o agotado")
    @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada")
    @ApiResponse(responseCode = "404", description = "El plato no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<PlatoResponseDTO> cambiarDisponibilidad(@PathVariable Long id,
                                                                  @Valid @RequestBody DisponibilidadRequestDTO request) {
        Plato plato = platoService.cambiarDisponibilidad(id, request.disponible());
        return ResponseEntity.ok(platoMapper.toResponse(plato));
    }

    @Operation(summary = "Eliminar un plato de la carta")
    @ApiResponse(responseCode = "204", description = "Plato eliminado")
    @ApiResponse(responseCode = "404", description = "El plato no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        platoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
