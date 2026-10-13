package com.restaurante.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// S10 - CORS: el navegador bloquea las peticiones desde otro dominio (ej. el front en localhost:5173)
// a menos que el servidor responda con las cabeceras Access-Control-Allow-*.
// Antes de la peticion real el navegador manda una pre solicitud (preflight OPTIONS).
// Aqui se habilita a nivel global; a nivel de controlador seria con @CrossOrigin.
// S08 - Los origenes salen de la propiedad cors.origenes leida con @Value.
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${cors.origenes}") List<String> origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenes);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
