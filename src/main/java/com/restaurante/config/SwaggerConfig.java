package com.restaurante.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// S08 - Swagger / OpenAPI. OpenAPI es el estandar (el contrato en JSON/YAML) y Swagger UI es la pagina que lo muestra.
// springdoc genera el contrato desde el codigo: si el codigo cambia, la documentacion cambia sola (adios Postman desactualizado).
// @OpenAPIDefinition personaliza titulo, version y descripcion. UI en /swagger-ui/index.html
// S10 - @SecurityScheme agrega el boton Authorize para mandar el token Bearer (JWT) o Basic desde Swagger.
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Blue Velvet API",
        version = "v1",
        description = "API REST de la cocteleria Blue Velvet: carta digital sincronizada con el inventario, "
                + "personalizacion de cocteles bajo reglas estrictas y tablero del bartender (KDS).",
        contact = @Contact(name = "Nicolas Alvarez")),
        security = {@SecurityRequirement(name = "bearerAuth"), @SecurityRequirement(name = "basicAuth")})
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class SwaggerConfig {
}
