package com.restaurante;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// S07 - Spring Boot: configuracion automatica, enfoque "obstinado" de la configuracion y app independiente
// (se ejecuta como un .jar con Tomcat embebido).
// @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan.
// El escaneo de componentes arranca desde ESTE paquete (com.restaurante) y baja por los subpaquetes,
// por eso no hace falta @EnableJpaRepositories ni @EnableMongoRepositories.
// Si la clase main estuviera en un subpaquete (ej. edu.dosw.parcial.main) toca poner
// scanBasePackages = "edu.dosw.parcial" y @EnableMongoRepositories(basePackages = "...persistence.repositories").
// S07 - Arquitectura cliente-servidor: el cliente PIDE (request) y este servidor RESPONDE (response) en JSON.
@SpringBootApplication
public class RestauranteApplication {

    public static void main(String[] args) {
        // S07 - Inversion de control (IoC): el framework toma el control, lee las anotaciones por reflection,
        // crea los beans (nosotros no usamos new) y los inyecta donde se necesitan (inyeccion de dependencias).
        SpringApplication.run(RestauranteApplication.class, args);
    }
}
