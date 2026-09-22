package com.restaurante.validator;

import com.restaurante.exception.ReglaNegocioException;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Modificador;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.domain.TipoBebida;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PedidoValidatorTest {

    private final PedidoValidator validator = new PedidoValidator();

    private final Coctel negroni = Coctel.builder().nombre("Negroni")
            .tipo(TipoBebida.ALCOHOLICA).disponible(true).build();
    private final Coctel virginMojito = Coctel.builder().nombre("Virgin Mojito")
            .tipo(TipoBebida.MOCKTAIL).disponible(true).build();

    @Test
    void coctelAgotadoLanzaAgotadoEnBarra() {
        Coctel agotado = Coctel.builder().nombre("Pina Colada").disponible(false).build();
        assertThatThrownBy(() -> validator.validarCoctelDisponible(agotado))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("AGOTADO EN BARRA");
    }

    @Test
    void coctelDisponibleEsValido() {
        assertThatCode(() -> validator.validarCoctelDisponible(negroni)).doesNotThrowAnyException();
    }

    @Test
    void alcoholicoSinDestiladoLanzaTrazabilidad() {
        assertThatThrownBy(() -> validator.validarTrazabilidad(negroni, null))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("destilado");
    }

    @Test
    void alcoholicoConDestiladoEsValido() {
        assertThatCode(() -> validator.validarTrazabilidad(negroni, "Tanqueray"))
                .doesNotThrowAnyException();
    }

    @Test
    void mocktailConDestiladoLanzaReglaNegocio() {
        assertThatThrownBy(() -> validator.validarTrazabilidad(virginMojito, "Ron"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Mocktail");
    }

    @Test
    void mocktailSinDestiladoEsValido() {
        assertThatCode(() -> validator.validarTrazabilidad(virginMojito, "")).doesNotThrowAnyException();
    }

    @Test
    void modificadorConAlcoholEnMocktailSeBloquea() {
        Modificador shot = Modificador.builder().nombre("Shot").graduacionAlcoholica(40.0).disponible(true).build();
        assertThatThrownBy(() -> validator.validarModificador(virginMojito, shot))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("tiene alcohol");
    }

    @Test
    void modificadorAgotadoSeBloquea() {
        Modificador agotado = Modificador.builder().nombre("Licor").graduacionAlcoholica(25.0).disponible(false).build();
        assertThatThrownBy(() -> validator.validarModificador(negroni, agotado))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("AGOTADO EN BARRA");
    }

    @Test
    void modificadorSinAlcoholEnMocktailEsValido() {
        Modificador jarabe = Modificador.builder().nombre("Agave").graduacionAlcoholica(0.0).disponible(true).build();
        assertThatCode(() -> validator.validarModificador(virginMojito, jarabe)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "RECIBIDO, EN_PREPARACION",
            "EN_PREPARACION, LISTO",
            "LISTO, ENTREGADO",
            "RECIBIDO, CANCELADO"
    })
    void transicionesValidas(EstadoPedido actual, EstadoPedido nuevo) {
        assertThatCode(() -> validator.validarTransicion(actual, nuevo)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "RECIBIDO, LISTO",
            "LISTO, EN_PREPARACION",
            "ENTREGADO, RECIBIDO",
            "EN_PREPARACION, CANCELADO",
            "CANCELADO, EN_PREPARACION"
    })
    void transicionesInvalidas(EstadoPedido actual, EstadoPedido nuevo) {
        assertThatThrownBy(() -> validator.validarTransicion(actual, nuevo))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void primerCambioDeLicorEnRecibidoEsValido() {
        Pedido pedido = Pedido.builder().estado(EstadoPedido.RECIBIDO).build();
        ItemPedido item = ItemPedido.builder().tipo(TipoBebida.ALCOHOLICA).cambiosDeLicor(0).build();
        assertThatCode(() -> validator.validarCambioDeLicor(pedido, item)).doesNotThrowAnyException();
    }

    @Test
    void segundoCambioDeLicorSeBloquea() {
        Pedido pedido = Pedido.builder().estado(EstadoPedido.RECIBIDO).build();
        ItemPedido item = ItemPedido.builder().tipo(TipoBebida.ALCOHOLICA).cambiosDeLicor(1).build();
        assertThatThrownBy(() -> validator.validarCambioDeLicor(pedido, item))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("una vez");
    }

    @Test
    void cambioDeLicorEnPreparacionSeBloquea() {
        Pedido pedido = Pedido.builder().estado(EstadoPedido.EN_PREPARACION).build();
        ItemPedido item = ItemPedido.builder().tipo(TipoBebida.ALCOHOLICA).cambiosDeLicor(0).build();
        assertThatThrownBy(() -> validator.validarCambioDeLicor(pedido, item))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("RECIBIDO");
    }

    @Test
    void cambioDeLicorEnMocktailSeBloquea() {
        Pedido pedido = Pedido.builder().estado(EstadoPedido.RECIBIDO).build();
        ItemPedido item = ItemPedido.builder().tipo(TipoBebida.MOCKTAIL).cambiosDeLicor(null).build();
        assertThatThrownBy(() -> validator.validarCambioDeLicor(pedido, item))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Mocktail");
    }
}
