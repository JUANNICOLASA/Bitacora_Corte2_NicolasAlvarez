package com.restaurante.config;

import com.restaurante.security.JwtAuthFilter;
import com.restaurante.security.ManejadorErroresSeguridad;
import com.restaurante.security.OAuth2LoginExitosoHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

// S10 - Seguridad con Spring Security.
// @EnableWebSecurity activa la cadena de filtros y @EnableMethodSecurity permite usar @PreAuthorize en los controllers.
// Ojo: con Spring Security en el classpath TODO queda bloqueado (401) hasta que se configure esta clase.
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // SecurityFilterChain: la cadena de filtros por la que pasa cada peticion antes de llegar al controller
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter,
                                           ManejadorErroresSeguridad manejadorErrores,
                                           CorsConfigurationSource corsConfigurationSource,
                                           OAuth2LoginExitosoHandler oAuth2LoginExitosoHandler,
                                           ObjectProvider<ClientRegistrationRepository> clientesOAuth2) throws Exception {
        http
                // CSRF deshabilitado: la API es stateless con JWT (no usa cookies de sesion), por eso no es vulnerable a CSRF
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // Cabeceras HTTP de seguridad:
                //  - Content-Security-Policy: limita de donde se cargan scripts y estilos (prevencion de XSS)
                //  - X-Frame-Options DENY / frame-ancestors 'none': nadie puede meter la app en un iframe (Clickjacking)
                //  - X-XSS-Protection: proteccion XSS de los navegadores viejos
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; "
                                        + "img-src 'self' data:; frame-ancestors 'none'"))
                        .frameOptions(frame -> frame.deny())
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)))
                // S07/S10 - Sistema SIN estado (stateless): el servidor no guarda sesion, cada peticion trae su token
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Securing URLs: que rutas son publicas (permitAll) y cuales piden estar autenticado.
                // Los permisos por rol mas finos estan en cada endpoint con @PreAuthorize.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/registro").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/menu", "/api/v1/menu/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/platos", "/api/v1/platos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/resenas", "/api/v1/resenas/resumen").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/reservas").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .anyRequest().authenticated())
                // 401 (no autenticado) y 403 (sin permisos) con el mismo ErrorResponseDTO del resto de la API
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(manejadorErrores)
                        .accessDeniedHandler(manejadorErrores))
                // HTTP Basic: usuario:password en Base64 en el header Authorization. Es solo codificacion, NO cifrado (usar con HTTPS)
                .httpBasic(basic -> basic.authenticationEntryPoint(manejadorErrores))
                // Nuestro filtro JWT corre antes del filtro de usuario/password de Spring
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        // OAuth2 (login con Google) solo se activa si existen las credenciales (perfil oauth2)
        if (clientesOAuth2.getIfAvailable() != null) {
            http.oauth2Login(oauth2 -> oauth2.successHandler(oAuth2LoginExitosoHandler));
        }
        return http.build();
    }

    // AuthenticationManager: valida email/contrasena usando UsuarioDetailsService + PasswordEncoder
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    // OWASP Cryptographic Failures: las contrasenas se guardan con hash BCrypt (con salt), nunca en texto plano
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
