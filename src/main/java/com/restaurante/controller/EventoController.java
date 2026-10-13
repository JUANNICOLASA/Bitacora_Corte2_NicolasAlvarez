package com.restaurante.controller;

import com.restaurante.mapper.EventoComandaMapper;
import com.restaurante.model.dto.response.EventoComandaResponseDTO;
import com.restaurante.service.EventoComandaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// S09 - Este controller consulta datos que viven en MongoDB. Al controller no le importa de donde
// vienen los datos, eso lo resuelve el service + repository.
@Tag(name = "Eventos", description = "Historial de eventos de las comandas guardado en MongoDB")
@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoComandaService eventoComandaService;
    private final EventoComandaMapper eventoComandaMapper;

    @Operation(summary = "Consultar el historial de eventos", description = "Se puede filtrar por comanda")
    @ApiResponse(responseCode = "200", description = "Eventos registrados")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<EventoComandaResponseDTO>> listar(
            @Parameter(description = "Filtrar por id de la comanda")
            // S07 - Query param para filtrar (no para identificar el recurso): /api/v1/eventos?idPedido=1
            @RequestParam(required = false) Long idPedido) {
        return ResponseEntity.ok(eventoComandaMapper.toResponseList(eventoComandaService.listar(idPedido)));
    }
}
