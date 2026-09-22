package com.restaurante.controller;

import com.restaurante.mapper.PedidoMapper;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.dto.request.CambioDestiladoRequestDTO;
import com.restaurante.model.dto.request.CambioEstadoRequestDTO;
import com.restaurante.model.dto.request.PedidoRequestDTO;
import com.restaurante.model.dto.response.ErrorResponseDTO;
import com.restaurante.model.dto.response.PedidoResponseDTO;
import com.restaurante.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Comandas enviadas al tablero del bartender (KDS).
 */
@Tag(name = "Pedidos", description = "Comandas y tablero del bartender (KDS)")
@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final PedidoMapper pedidoMapper;

    @Operation(summary = "Enviar una comanda a la barra",
            description = "Valida disponibilidad, trazabilidad del destilado y restriccion de Mocktails")
    @ApiResponse(responseCode = "201", description = "Comanda creada en estado RECIBIDO")
    @ApiResponse(responseCode = "400", description = "Datos invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Coctel o modificador no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "422", description = "Agotado en barra, falta el destilado o modificador con alcohol en Mocktail",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crear(@Valid @RequestBody PedidoRequestDTO request) {
        Pedido creado = pedidoService.crear(pedidoMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoMapper.toResponse(creado));
    }

    @Operation(summary = "Listar comandas", description = "Se puede filtrar por estado")
    @ApiResponse(responseCode = "200", description = "Lista de comandas")
    @ApiResponse(responseCode = "400", description = "Estado invalido",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listar(
            @Parameter(description = "Filtrar por estado")
            @RequestParam(required = false) EstadoPedido estado) {
        List<Pedido> pedidos = estado == null ? pedidoService.listar() : pedidoService.listarPorEstado(estado);
        return ResponseEntity.ok(pedidoMapper.toResponseList(pedidos));
    }

    @Operation(summary = "Tablero del bartender: comandas activas en orden de llegada")
    @ApiResponse(responseCode = "200", description = "Comandas que no estan ENTREGADAS ni CANCELADAS")
    @GetMapping("/activos")
    public ResponseEntity<List<PedidoResponseDTO>> listarActivos() {
        return ResponseEntity.ok(pedidoMapper.toResponseList(pedidoService.listarActivos()));
    }

    @Operation(summary = "Obtener una comanda por id")
    @ApiResponse(responseCode = "200", description = "Comanda encontrada")
    @ApiResponse(responseCode = "404", description = "La comanda no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoMapper.toResponse(pedidoService.obtenerPorId(id)));
    }

    @Operation(summary = "Avanzar el estado de la comanda en el KDS",
            description = "RECIBIDO -> EN_PREPARACION -> LISTO -> ENTREGADO")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Estado invalido",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "La comanda no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "422", description = "Transicion invalida",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(@PathVariable Long id,
                                                           @Valid @RequestBody CambioEstadoRequestDTO request) {
        return ResponseEntity.ok(pedidoMapper.toResponse(pedidoService.cambiarEstado(id, request.estado())));
    }

    @Operation(summary = "Cambiar el licor de un coctel de la comanda",
            description = "Solo una vez por coctel y solo mientras la comanda esta en RECIBIDO")
    @ApiResponse(responseCode = "200", description = "Licor actualizado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "La comanda o el item no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "422", description = "Ya se cambio una vez, ya esta en preparacion o es Mocktail",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PatchMapping("/{id}/items/{idItem}/destilado")
    public ResponseEntity<PedidoResponseDTO> cambiarDestilado(@PathVariable Long id,
                                                              @PathVariable Long idItem,
                                                              @Valid @RequestBody CambioDestiladoRequestDTO request) {
        Pedido pedido = pedidoService.cambiarDestilado(id, idItem, request.destilado());
        return ResponseEntity.ok(pedidoMapper.toResponse(pedido));
    }

    @Operation(summary = "Cancelar una comanda", description = "Solo si esta en RECIBIDO")
    @ApiResponse(responseCode = "204", description = "Comanda cancelada")
    @ApiResponse(responseCode = "404", description = "La comanda no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "422", description = "La comanda ya esta en barra",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        pedidoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
