package com.restaurante.config;

import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.Modificador;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.service.CoctelService;
import com.restaurante.service.ModificadorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final CoctelService coctelService;
    private final ModificadorService modificadorService;

    @Override
    public void run(String... args) {
        if (!coctelService.listar().isEmpty()) {
            log.info("La carta de Blue Velvet ya tiene datos, no se carga la carta inicial");
            return;
        }
        coctelService.crear(coctel("Blue Velvet Signature",
                "Gin, licor de mora azul, limon y espuma de lavanda", 42000.0,
                CategoriaCoctel.DE_AUTOR, TipoBebida.ALCOHOLICA, "Gin", true));
        coctelService.crear(coctel("Negroni", "Gin, vermut rojo y Campari", 38000.0,
                CategoriaCoctel.CLASICO, TipoBebida.ALCOHOLICA, "Gin", true));
        coctelService.crear(coctel("Old Fashioned", "Whisky bourbon, azucar y amargo de angostura", 40000.0,
                CategoriaCoctel.CLASICO, TipoBebida.ALCOHOLICA, "Whisky", true));
        coctelService.crear(coctel("Whisky Sour", "Whisky, limon, jarabe simple y clara de huevo", 36000.0,
                CategoriaCoctel.SOUR, TipoBebida.ALCOHOLICA, "Whisky", true));
        coctelService.crear(coctel("Pina Colada", "Ron blanco, crema de coco y pina", 34000.0,
                CategoriaCoctel.TROPICAL, TipoBebida.ALCOHOLICA, "Ron", false));
        coctelService.crear(coctel("Virgin Mojito", "Hierbabuena, limon, azucar y soda", 22000.0,
                CategoriaCoctel.SIN_ALCOHOL, TipoBebida.MOCKTAIL, null, true));
        coctelService.crear(coctel("Velvet Garden", "Te de flor de jamaica, pepino y tonica", 24000.0,
                CategoriaCoctel.SIN_ALCOHOL, TipoBebida.MOCKTAIL, null, true));

        modificadorService.crear(modificador("Shot extra de destilado", 40.0, 9000.0, true));
        modificadorService.crear(modificador("Float de ron oscuro", 40.0, 8000.0, true));
        modificadorService.crear(modificador("Jarabe de agave", 0.0, 2000.0, true));
        modificadorService.crear(modificador("Twist de naranja", 0.0, 0.0, true));
        modificadorService.crear(modificador("Espuma de lavanda", 0.0, 3000.0, true));
        modificadorService.crear(modificador("Licor de mora azul", 25.0, 6000.0, false));

        log.info("Carta inicial de Blue Velvet cargada: {} cocteles, {} modificadores",
                coctelService.listar().size(), modificadorService.listar().size());
    }

    private Coctel coctel(String nombre, String descripcion, Double precio, CategoriaCoctel categoria,
                          TipoBebida tipo, String destiladoBase, boolean disponible) {
        return Coctel.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .precio(precio)
                .categoria(categoria)
                .tipo(tipo)
                .destiladoBase(destiladoBase)
                .disponible(disponible)
                .build();
    }

    private Modificador modificador(String nombre, Double graduacion, Double precioExtra, boolean disponible) {
        return Modificador.builder()
                .nombre(nombre)
                .graduacionAlcoholica(graduacion)
                .precioExtra(precioExtra)
                .disponible(disponible)
                .build();
    }
}
