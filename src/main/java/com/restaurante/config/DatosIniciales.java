package com.restaurante.config;

import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.CategoriaPlato;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.Modificador;
import com.restaurante.model.domain.Plato;
import com.restaurante.model.domain.Rol;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.model.domain.Usuario;
import com.restaurante.service.AuthService;
import com.restaurante.service.CoctelService;
import com.restaurante.service.ModificadorService;
import com.restaurante.service.PlatoService;
import com.restaurante.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// S07 - @Component: bean generico de Spring. CommandLineRunner se ejecuta cuando arranca la aplicacion.
// S08 - @Profile("!test"): solo corre cuando NO esta activo el perfil test (perfiles por ambiente).
// S10 - La contrasena de los usuarios iniciales viene de una variable de entorno, nunca quemada en el codigo.
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final CoctelService coctelService;
    private final ModificadorService modificadorService;
    private final PlatoService platoService;
    private final AuthService authService;

    @Value("${usuarios.password-inicial:}")
    private String passwordInicial;

    @Override
    public void run(String... args) {
        cargarUsuarios();
        cargarCarta();
    }

    private void cargarUsuarios() {
        if (passwordInicial.isBlank()) {
            // S08 - Log WARN: situacion rara que no rompe la aplicacion
            log.warn("No se definio USUARIOS_PASSWORD, no se crean los usuarios iniciales");
            return;
        }
        crearUsuario("Administrador", "admin@bluevelvet.com", Rol.ADMIN);
        crearUsuario("Bartender", "bartender@bluevelvet.com", Rol.BARTENDER);
        crearUsuario("Mesero", "mesero@bluevelvet.com", Rol.MESERO);
        crearUsuario("Cliente", "cliente@bluevelvet.com", Rol.CLIENTE);
    }

    private void crearUsuario(String nombre, String email, Rol rol) {
        if (!authService.existe(email)) {
            authService.crear(Usuario.builder().nombre(nombre).email(email).password(passwordInicial).rol(rol).build());
        }
    }

    private void cargarCarta() {
        Set<String> cocteles = nombres(coctelService.listar().stream().map(Coctel::getNombre));
        List<Coctel> nuevosCocteles = Stream.of(
                        coctel("Blue Velvet Signature", "Gin, licor de mora azul, limon y espuma de lavanda", 42000.0,
                                CategoriaCoctel.DE_AUTOR, TipoBebida.ALCOHOLICA, "Gin", true),
                        coctel("Biblioteca Prohibida", "Whisky ahumado, miel de flores, té negro y un toque de cuero, "
                                + "servido bajo una campana de humo.", 46000.0,
                                CategoriaCoctel.DE_AUTOR, TipoBebida.ALCOHOLICA, "Whisky escocés ahumado", true),
                        coctel("Cortina Roja", "Mezcal, frutos rojos, ají dulce y sal de flor de Jamaica.", 44000.0,
                                CategoriaCoctel.DE_AUTOR, TipoBebida.ALCOHOLICA, "Mezcal espadín", true),
                        coctel("Tinta de Medianoche", "Ron oscuro añejo, café de origen, cacao amargo y naranja.",
                                40000.0, CategoriaCoctel.DE_AUTOR, TipoBebida.ALCOHOLICA, "Ron oscuro añejo", true),
                        coctel("Última Función", "Ginebra, licor de violeta, marrasquino y limón.", 41000.0,
                                CategoriaCoctel.DE_AUTOR, TipoBebida.ALCOHOLICA, "Ginebra", true),
                        coctel("Negroni", "Gin, vermut rojo y Campari", 38000.0,
                                CategoriaCoctel.CLASICO, TipoBebida.ALCOHOLICA, "Gin", true),
                        coctel("Old Fashioned", "Whisky bourbon, azucar y amargo de angostura", 40000.0,
                                CategoriaCoctel.CLASICO, TipoBebida.ALCOHOLICA, "Whisky", true),
                        coctel("Espresso Martini", "Vodka, espresso recién extraído y licor de café.", 37000.0,
                                CategoriaCoctel.CLASICO, TipoBebida.ALCOHOLICA, "Vodka", true),
                        coctel("Whisky Sour", "Whisky, limon, jarabe simple y clara de huevo", 36000.0,
                                CategoriaCoctel.SOUR, TipoBebida.ALCOHOLICA, "Whisky", true),
                        coctel("Pina Colada", "Ron blanco, crema de coco y pina", 34000.0,
                                CategoriaCoctel.TROPICAL, TipoBebida.ALCOHOLICA, "Ron", false),
                        coctel("Virgin Mojito", "Hierbabuena, limon, azucar y soda", 22000.0,
                                CategoriaCoctel.SIN_ALCOHOL, TipoBebida.MOCKTAIL, null, true),
                        coctel("Velvet Garden", "Te de flor de jamaica, pepino y tonica", 24000.0,
                                CategoriaCoctel.SIN_ALCOHOL, TipoBebida.MOCKTAIL, null, true))
                .filter(coctel -> !cocteles.contains(TextoUtil.normalizar(coctel.getNombre())))
                .toList();
        nuevosCocteles.forEach(coctelService::crear);

        Set<String> modificadores = nombres(modificadorService.listar().stream().map(Modificador::getNombre));
        List<Modificador> nuevosModificadores = Stream.of(
                        modificador("Shot extra de destilado", 40.0, 9000.0, true),
                        modificador("Float de ron oscuro", 40.0, 8000.0, true),
                        modificador("Jarabe de agave", 0.0, 2000.0, true),
                        modificador("Twist de naranja", 0.0, 0.0, true),
                        modificador("Espuma de lavanda", 0.0, 3000.0, true),
                        modificador("Licor de mora azul", 25.0, 6000.0, false))
                .filter(modificador -> !modificadores.contains(TextoUtil.normalizar(modificador.getNombre())))
                .toList();
        nuevosModificadores.forEach(modificadorService::crear);

        Set<String> platos = nombres(platoService.listar().stream().map(Plato::getNombre));
        List<Plato> nuevosPlatos = Stream.of(
                        plato("Tabla del archivista", "Quesos madurados, embutidos curados, miel de trufa y pan de "
                                + "masa madre.", 58000.0, CategoriaPlato.PARA_COMPARTIR, "Para 2 a 3 personas", true),
                        plato("Croquetas de jamón serrano", "Bechamel cremosa, jamón serrano y alioli de ajo negro.",
                                28000.0, CategoriaPlato.PARA_COMPARTIR, "6 unidades", true),
                        plato("Tostadas de atún sellado", "Atún rojo sellado, aguacate, ponzu cítrico y ajonjolí negro.",
                                34000.0, CategoriaPlato.PARA_COMPARTIR, "4 unidades", true),
                        plato("Papas trufadas", "Papas rústicas, aceite de trufa, parmesano y perejil.", 22000.0,
                                CategoriaPlato.PARA_COMPARTIR, "Para compartir", true),
                        plato("Lomo al vino tinto", "Lomo de res, reducción de vino tinto, puré de papa ahumado.",
                                72000.0, CategoriaPlato.PLATOS_FUERTES, "300 g", true),
                        plato("Risotto de hongos negros", "Arroz arborio, hongos silvestres, tinta de calamar y "
                                + "parmesano.", 54000.0, CategoriaPlato.PLATOS_FUERTES, "Individual", true),
                        plato("Hamburguesa Velvet", "Pan negro de carbón activado, res madurada, queso azul y cebolla "
                                + "caramelizada.", 46000.0, CategoriaPlato.PLATOS_FUERTES, "200 g, con papas", true),
                        plato("Pulpo a la brasa", "Pulpo braseado, papa criolla, pimentón de la Vera y alioli.",
                                68000.0, CategoriaPlato.PLATOS_FUERTES, "Individual", false),
                        plato("Volcán de chocolate amargo", "Chocolate al 70 %, centro líquido y helado de vainilla.",
                                24000.0, CategoriaPlato.POSTRES, "Individual", true),
                        plato("Crème brûlée de lavanda", "Crema infusionada con lavanda y costra de azúcar quemado.",
                                22000.0, CategoriaPlato.POSTRES, "Individual", true),
                        plato("Cheesecake de frutos rojos", "Cheesecake horneado, base de galleta y coulis de frutos "
                                + "rojos.", 21000.0, CategoriaPlato.POSTRES, "Individual", true))
                .filter(plato -> !platos.contains(TextoUtil.normalizar(plato.getNombre())))
                .toList();
        nuevosPlatos.forEach(platoService::crear);

        log.info("Carta inicial de Blue Velvet al dia: {} cocteles, {} modificadores y {} platos nuevos",
                nuevosCocteles.size(), nuevosModificadores.size(), nuevosPlatos.size());
    }

    private Set<String> nombres(Stream<String> nombres) {
        return nombres.map(TextoUtil::normalizar).collect(Collectors.toSet());
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

    private Plato plato(String nombre, String descripcion, Double precio, CategoriaPlato categoria, String porcion,
                        boolean disponible) {
        return Plato.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .precio(precio)
                .categoria(categoria)
                .porcion(porcion)
                .disponible(disponible)
                .build();
    }
}
