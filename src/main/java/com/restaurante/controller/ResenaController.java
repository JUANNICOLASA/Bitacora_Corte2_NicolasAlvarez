package com.restaurante.controller;

import com.restaurante.mapper.ResenaMapper;
import com.restaurante.model.domain.Resena;
import com.restaurante.model.domain.TipoProducto;
import com.restaurante.model.dto.request.ResenaRequestDTO;
import com.restaurante.model.dto.response.ErrorResponseDTO;
import com.restaurante.model.dto.response.ResenaResponseDTO;
import com.restaurante.model.dto.response.ResumenCalificacionResponseDTO;
import com.restaurante.service.ResenaService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Resenas", description = "Calificaciones de los clientes guardadas en MongoDB")
@RestController
@RequestMapping("/api/v1/resenas")
@RequiredArgsConstructor
public class ResenaController {

    private final ResenaService resenaService;
    private final ResenaMapper resenaMapper;

    @Operation(summary = "Ver las opiniones de un producto", description = "Ordenadas de la mas reciente a la mas antigua")
    @ApiResponse(responseCode = "200", description = "Opiniones del producto")
    @ApiResponse(responseCode = "400", description = "Tipo de producto invalido o parametros faltantes",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @SecurityRequirements
    @GetMapping
    public ResponseEntity<List<ResenaResponseDTO>> listar(
            @Parameter(description = "COCTEL o PLATO") @RequestParam TipoProducto tipo,
            @Parameter(description = "Id del producto") @RequestParam Long idProducto) {
        return ResponseEntity.ok(resenaMapper.toResponseList(resenaService.listar(tipo, idProducto)));
    }

    @Operation(summary = "Ver el promedio de calificaciones de cada producto")
    @ApiResponse(responseCode = "200", description = "Resumen por producto")
    @SecurityRequirements
    @GetMapping("/resumen")
    public ResponseEntity<List<ResumenCalificacionResponseDTO>> resumen() {
        return ResponseEntity.ok(resenaMapper.toResumenResponseList(resenaService.resumen()));
    }

    @Operation(summary = "Calificar un coctel o un plato", description = "Cada cliente califica una vez cada producto")
    @ApiResponse(responseCode = "201", description = "Opinion publicada")
    @ApiResponse(responseCode = "400", description = "Datos invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "El producto no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "El cliente ya califico el producto",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping
    public ResponseEntity<ResenaResponseDTO> crear(@Valid @RequestBody ResenaRequestDTO request,
                                                   Authentication usuario) {
        Resena creada = resenaService.crear(resenaMapper.toDomain(request), usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(resenaMapper.toResponse(creada));
    }
}
