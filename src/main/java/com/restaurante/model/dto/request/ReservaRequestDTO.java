package com.restaurante.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Datos para reservar una mesa")
public record ReservaRequestDTO(

        @Schema(example = "Laura Gomez")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
        String nombre,

        @Schema(example = "300 555 0186")
        @NotBlank(message = "El telefono es obligatorio")
        @Pattern(regexp = "^[0-9+\\s()-]{7,20}$", message = "El telefono debe tener al menos 7 digitos")
        String telefono,

        @Schema(example = "laura@correo.com")
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        @Size(max = 120, message = "El correo no puede superar 120 caracteres")
        String correo,

        @Schema(description = "Dia de la visita", example = "2026-12-04")
        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha,

        @Schema(description = "Hora de llegada, de 18:00 a 00:00 cada media hora", example = "20:00")
        @NotNull(message = "La hora es obligatoria")
        LocalTime hora,

        @Schema(description = "Numero de personas", example = "2")
        @NotNull(message = "El numero de personas es obligatorio")
        @Min(value = 1, message = "La reserva debe ser minimo para 1 persona")
        @Max(value = 12, message = "La reserva puede ser maximo para 12 personas")
        Integer personas,

        @Schema(example = "Aniversario")
        @Size(max = 40, message = "La ocasion no puede superar 40 caracteres")
        String ocasion,

        @Schema(example = "Una mesa cerca del escenario")
        @Size(max = 300, message = "Las notas no pueden superar 300 caracteres")
        String notas
) {
}
