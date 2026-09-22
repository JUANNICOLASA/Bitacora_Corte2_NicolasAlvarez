package com.restaurante.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Informacion general de la documentacion OpenAPI.
 * Swagger UI: http://localhost:8080/swagger-ui/index.html
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Blue Velvet API",
        version = "v1",
        description = "API REST de la cocteleria Blue Velvet: carta digital sincronizada con el inventario, "
                + "personalizacion de cocteles bajo reglas estrictas y tablero del bartender (KDS).",
        contact = @Contact(name = "Nicolas Alvarez")))
public class SwaggerConfig {
}
