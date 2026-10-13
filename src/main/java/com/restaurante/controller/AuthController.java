package com.restaurante.controller;

import com.restaurante.mapper.UsuarioMapper;
import com.restaurante.model.domain.Usuario;
import com.restaurante.model.dto.request.LoginRequestDTO;
import com.restaurante.model.dto.request.RegistroRequestDTO;
import com.restaurante.model.dto.response.ErrorResponseDTO;
import com.restaurante.model.dto.response.TokenResponseDTO;
import com.restaurante.model.dto.response.UsuarioResponseDTO;
import com.restaurante.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// S10 - Autenticacion ("quien eres tu?"): el login devuelve un JWT que el cliente manda en cada peticion
// en el header Authorization: Bearer <token>.
@Tag(name = "Autenticacion", description = "Inicio de sesion con JWT y registro de clientes")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UsuarioMapper usuarioMapper;

    @Operation(summary = "Iniciar sesion y obtener un token JWT")
    @ApiResponse(responseCode = "200", description = "Token generado")
    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "429", description = "Demasiados intentos fallidos",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    // @SecurityRequirements vacio = endpoint publico en Swagger (sin candado)
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(TokenResponseDTO.bearer(authService.login(request.email(), request.password())));
    }

    // POST que crea -> 201 Created
    @Operation(summary = "Registrar un cliente")
    @ApiResponse(responseCode = "201", description = "Cliente registrado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos o contrasena debil",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "El correo ya esta registrado",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @SecurityRequirements
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody RegistroRequestDTO request) {
        Usuario creado = authService.registrar(usuarioMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioMapper.toResponse(creado));
    }

    @Operation(summary = "Consultar el usuario autenticado")
    @ApiResponse(responseCode = "200", description = "Usuario autenticado")
    @ApiResponse(responseCode = "401", description = "Sin token o token invalido",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> me(Authentication usuario) {
        return ResponseEntity.ok(usuarioMapper.toResponse(authService.obtenerPorEmail(usuario.getName())));
    }
}
