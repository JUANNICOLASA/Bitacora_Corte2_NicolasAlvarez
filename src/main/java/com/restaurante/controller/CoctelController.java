package com.restaurante.controller;

import com.restaurante.mapper.CoctelMapper;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.dto.request.CoctelRequestDTO;
import com.restaurante.model.dto.request.DisponibilidadRequestDTO;
import com.restaurante.model.dto.response.CoctelResponseDTO;
import com.restaurante.model.dto.response.ErrorResponseDTO;
import com.restaurante.service.CoctelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Cocteles", description = "Administracion de la carta de cocteles")
@RestController
@RequestMapping("/api/v1/cocteles")
@RequiredArgsConstructor
public class CoctelController {

    private final CoctelService coctelService;
    private final CoctelMapper coctelMapper;

    @Operation(summary = "Listar todos los cocteles")
    @ApiResponse(responseCode = "200", description = "Lista de cocteles")
    @GetMapping
    public ResponseEntity<List<CoctelResponseDTO>> listar() {
        return ResponseEntity.ok(coctelMapper.toResponseList(coctelService.listar()));
    }

    @Operation(summary = "Obtener un coctel por id")
    @ApiResponse(responseCode = "200", description = "Coctel encontrado")
    @ApiResponse(responseCode = "404", description = "El coctel no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @GetMapping("/{id}")
    public ResponseEntity<CoctelResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(coctelMapper.toResponse(coctelService.obtenerPorId(id)));
    }

    @Operation(summary = "Crear un coctel")
    @ApiResponse(responseCode = "201", description = "Coctel creado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "Nombre duplicado",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "422", description = "Alcoholico sin destilado base o Mocktail con destilado",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PostMapping
    public ResponseEntity<CoctelResponseDTO> crear(@Valid @RequestBody CoctelRequestDTO request) {
        Coctel creado = coctelService.crear(coctelMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(coctelMapper.toResponse(creado));
    }

    @Operation(summary = "Actualizar un coctel completo")
    @ApiResponse(responseCode = "200", description = "Coctel actualizado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "El coctel no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "Nombre duplicado",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PutMapping("/{id}")
    public ResponseEntity<CoctelResponseDTO> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody CoctelRequestDTO request) {
        Coctel actualizado = coctelService.actualizar(id, coctelMapper.toDomain(request));
        return ResponseEntity.ok(coctelMapper.toResponse(actualizado));
    }

    @Operation(summary = "Marcar un coctel como disponible o agotado en barra")
    @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada")
    @ApiResponse(responseCode = "400", description = "Datos invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "El coctel no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<CoctelResponseDTO> cambiarDisponibilidad(@PathVariable Long id,
                                                                   @Valid @RequestBody DisponibilidadRequestDTO request) {
        Coctel coctel = coctelService.cambiarDisponibilidad(id, request.disponible());
        return ResponseEntity.ok(coctelMapper.toResponse(coctel));
    }

    @Operation(summary = "Eliminar un coctel de la carta")
    @ApiResponse(responseCode = "204", description = "Coctel eliminado")
    @ApiResponse(responseCode = "404", description = "El coctel no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "El coctel tiene comandas registradas",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        coctelService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
