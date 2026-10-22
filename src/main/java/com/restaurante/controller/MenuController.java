package com.restaurante.controller;

import com.restaurante.mapper.MenuMapper;
import com.restaurante.mapper.RecetaExternaMapper;
import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.dto.response.ErrorResponseDTO;
import com.restaurante.model.dto.response.MenuItemResponseDTO;
import com.restaurante.model.dto.response.ModificadorMenuResponseDTO;
import com.restaurante.model.dto.response.RecetaExternaResponseDTO;
import com.restaurante.service.CoctelService;
import com.restaurante.service.ModificadorService;
import com.restaurante.service.RecetaExternaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// S08 - Menu vs Coctel: es la misma informacion pero con distinta semantica. El menu es lo que ve el cliente
// (publico, sin token) y /cocteles es lo que administra el ADMIN. Se reutiliza el mismo servicio
// con distintos endpoints, rutas y validaciones.
@Tag(name = "Menu", description = "Carta digital sincronizada con el inventario de barra")
@SecurityRequirements
@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
public class MenuController {

    // Un controller puede usar varios services (siempre por la interfaz)
    private final CoctelService coctelService;
    private final ModificadorService modificadorService;
    private final MenuMapper menuMapper;
    private final RecetaExternaService recetaExternaService;
    private final RecetaExternaMapper recetaExternaMapper;

    @Operation(summary = "Ver la carta completa",
            description = "Incluye los cocteles agotados con la etiqueta AGOTADO EN BARRA y seleccionable=false")
    @ApiResponse(responseCode = "200", description = "Carta")
    @ApiResponse(responseCode = "400", description = "Categoria invalida",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @GetMapping
    public ResponseEntity<List<MenuItemResponseDTO>> verCarta(
            @Parameter(description = "Filtrar por categoria")
            @RequestParam(required = false) CategoriaCoctel categoria) {
        List<Coctel> cocteles = categoria == null
                ? coctelService.listar()
                : coctelService.listarPorCategoria(categoria);
        return ResponseEntity.ok(menuMapper.toMenuItems(cocteles));
    }

    @Operation(summary = "Ver solo los cocteles disponibles en barra")
    @ApiResponse(responseCode = "200", description = "Cocteles disponibles")
    @GetMapping("/disponibles")
    public ResponseEntity<List<MenuItemResponseDTO>> verDisponibles() {
        return ResponseEntity.ok(menuMapper.toMenuItems(coctelService.obtenerDisponibles()));
    }

    @Operation(summary = "Ver el detalle de un coctel de la carta")
    @ApiResponse(responseCode = "200", description = "Detalle del coctel")
    @ApiResponse(responseCode = "404", description = "El coctel no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponseDTO> verDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(menuMapper.toMenuItem(coctelService.obtenerPorId(id)));
    }

    @Operation(summary = "Ver los modificadores para personalizar un coctel",
            description = "Si el coctel es Mocktail, los modificadores con alcohol salen con habilitado=false")
    @ApiResponse(responseCode = "200", description = "Modificadores con su estado")
    @ApiResponse(responseCode = "404", description = "El coctel no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @GetMapping("/{id}/modificadores")
    public ResponseEntity<List<ModificadorMenuResponseDTO>> verModificadores(@PathVariable Long id) {
        Coctel coctel = coctelService.obtenerPorId(id);
        return ResponseEntity.ok(menuMapper.toModificadoresMenu(modificadorService.listar(), coctel));
    }

    @Operation(summary = "Ver la receta de referencia de un coctel",
            description = "Se consulta en TheCocktailDB por medio de un cliente Feign")
    @ApiResponse(responseCode = "200", description = "Receta de referencia")
    @ApiResponse(responseCode = "404", description = "El coctel no existe o no tiene receta de referencia",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "502", description = "El proveedor externo no responde",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @GetMapping("/{id}/receta")
    public ResponseEntity<RecetaExternaResponseDTO> verReceta(@PathVariable Long id) {
        return ResponseEntity.ok(recetaExternaMapper.toResponse(recetaExternaService.buscarPorCoctel(id)));
    }
}
