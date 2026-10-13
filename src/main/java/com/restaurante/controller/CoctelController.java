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
import org.springframework.security.access.prepost.PreAuthorize;
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

// S07 - Controller = "el mesero": recibe la peticion HTTP, llama al Service y devuelve el ResponseDTO.
// No tiene logica de negocio y nunca habla directamente con la base de datos.
// @RestController = @Controller + @ResponseBody (todo lo que retorna se convierte a JSON).
// S08 - Versionamiento en la URL: /api/v1/... desde el primer endpoint (visible en Swagger y facil de probar).
// S07 - Buenas practicas REST: recursos como sustantivos en plural (/cocteles) y jerarquia padre-hijo
// (/cocteles/{id}/disponibilidad).
// S08 - @Tag, @Operation y @ApiResponse documentan cada endpoint en Swagger.
@Tag(name = "Cocteles", description = "Administracion de la carta de cocteles")
@RestController
@RequestMapping("/api/v1/cocteles")
@RequiredArgsConstructor
public class CoctelController {

    // S07 - Inversion de dependencias: depende de la interfaz CoctelService, no de CoctelServiceImpl.
    // Inyeccion por constructor: @RequiredArgsConstructor (Lombok) genera el constructor con los campos final.
    private final CoctelService coctelService;
    private final CoctelMapper coctelMapper;

    // GET: metodo seguro (no modifica) e idempotente -> 200 OK
    @Operation(summary = "Listar todos los cocteles")
    @ApiResponse(responseCode = "200", description = "Lista de cocteles")
    // S10 - Autorizacion ("que tienes permitido hacer?"): @PreAuthorize evalua el rol del token antes de ejecutar
    // el metodo. Si el rol no alcanza -> 403 Forbidden.
    @PreAuthorize("hasAnyRole('ADMIN','BARTENDER')")
    @GetMapping
    public ResponseEntity<List<CoctelResponseDTO>> listar() {
        return ResponseEntity.ok(coctelMapper.toResponseList(coctelService.listar()));
    }

    @Operation(summary = "Obtener un coctel por id")
    @ApiResponse(responseCode = "200", description = "Coctel encontrado")
    @ApiResponse(responseCode = "404", description = "El coctel no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PreAuthorize("hasAnyRole('ADMIN','BARTENDER')")
    @GetMapping("/{id}")
    public ResponseEntity<CoctelResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(coctelMapper.toResponse(coctelService.obtenerPorId(id)));
    }

    // POST: crea un recurso y NO es idempotente (N llamadas = N cocteles) -> 201 Created
    // 400 = el formato esta mal (Bean Validation), 409 = nombre duplicado,
    // 422 = el formato esta bien pero el negocio lo rechaza (ej. alcoholico sin destilado base)
    @Operation(summary = "Crear un coctel")
    @ApiResponse(responseCode = "201", description = "Coctel creado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "Nombre duplicado",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "422", description = "Alcoholico sin destilado base o Mocktail con destilado",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    // @Valid activa las validaciones de input del DTO. Si fallan -> MethodArgumentNotValidException -> 400
    // Flujo: RequestDTO --(mapper IN)--> dominio --> service --> dominio --(mapper IN)--> ResponseDTO
    public ResponseEntity<CoctelResponseDTO> crear(@Valid @RequestBody CoctelRequestDTO request) {
        Coctel creado = coctelService.crear(coctelMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(coctelMapper.toResponse(creado));
    }

    // PUT: reemplaza el recurso completo. Es idempotente: 1 o N veces deja el mismo resultado -> 200
    @Operation(summary = "Actualizar un coctel completo")
    @ApiResponse(responseCode = "200", description = "Coctel actualizado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "El coctel no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "Nombre duplicado",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CoctelResponseDTO> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody CoctelRequestDTO request) {
        Coctel actualizado = coctelService.actualizar(id, coctelMapper.toDomain(request));
        return ResponseEntity.ok(coctelMapper.toResponse(actualizado));
    }

    // PATCH: modificacion parcial, solo cambia el campo disponible
    @Operation(summary = "Marcar un coctel como disponible o agotado en barra")
    @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada")
    @ApiResponse(responseCode = "400", description = "Datos invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "El coctel no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PreAuthorize("hasAnyRole('ADMIN','BARTENDER')")
    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<CoctelResponseDTO> cambiarDisponibilidad(@PathVariable Long id,
                                                                   @Valid @RequestBody DisponibilidadRequestDTO request) {
        Coctel coctel = coctelService.cambiarDisponibilidad(id, request.disponible());
        return ResponseEntity.ok(coctelMapper.toResponse(coctel));
    }

    // DELETE: idempotente, responde 204 No Content (sin body). 404 = no existe vs 409 = tiene comandas
    @Operation(summary = "Eliminar un coctel de la carta")
    @ApiResponse(responseCode = "204", description = "Coctel eliminado")
    @ApiResponse(responseCode = "404", description = "El coctel no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "El coctel tiene comandas registradas",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        coctelService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
