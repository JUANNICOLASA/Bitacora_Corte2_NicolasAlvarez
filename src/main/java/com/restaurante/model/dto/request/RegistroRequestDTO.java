package com.restaurante.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar un cliente")
public record RegistroRequestDTO(

        @Schema(example = "Laura Gomez")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre no puede superar 80 caracteres")
        String nombre,

        @Schema(example = "laura@correo.com")
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        @Size(max = 120, message = "El correo no puede superar 120 caracteres")
        String email,

        @Schema(example = "Velvet2026*")
        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, max = 64, message = "La contrasena debe tener entre 8 y 64 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "La contrasena debe tener mayuscula, minuscula, numero y caracter especial")
        String password
) {
}
