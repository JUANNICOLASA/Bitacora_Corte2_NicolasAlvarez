package com.restaurante.validator;

import com.restaurante.exception.ReglaNegocioException;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Modificador;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.util.AccesibilidadUtil;
import com.restaurante.util.TextoUtil;
import org.springframework.stereotype.Component;

/**
 * Reglas de negocio de las comandas de Blue Velvet.
 */
@Component
public class PedidoValidator {

    public static final int MAX_CAMBIOS_DE_LICOR = 1;

    /**
     * El inventario manda: no se puede pedir un coctel agotado.
     */
    public void validarCoctelDisponible(Coctel coctel) {
        if (!coctel.estaDisponible()) {
            throw new ReglaNegocioException(
                    "El coctel " + coctel.getNombre() + " esta " + AccesibilidadUtil.ETIQUETA_AGOTADO);
        }
    }

    /**
     * Trazabilidad del alcohol: ningun coctel alcoholico entra a la comanda sin marca o tipo
     * exacto de destilado. Un Mocktail no lleva destilado.
     */
    public void validarTrazabilidad(Coctel coctel, String destilado) {
        boolean sinDestilado = TextoUtil.estaVacio(destilado);
        if (coctel.getTipo() == TipoBebida.ALCOHOLICA && sinDestilado) {
            throw new ReglaNegocioException("El coctel " + coctel.getNombre()
                    + " requiere especificar la marca o tipo exacto de destilado");
        }
        if (coctel.esMocktail() && !sinDestilado) {
            throw new ReglaNegocioException("El coctel " + coctel.getNombre()
                    + " es Mocktail y no puede llevar destilado");
        }
    }

    /**
     * Restriccion de Mocktails: se bloquea cualquier modificador con graduacion alcoholica.
     * Tampoco se permiten modificadores agotados.
     */
    public void validarModificador(Coctel coctel, Modificador modificador) {
        if (!modificador.estaDisponible()) {
            throw new ReglaNegocioException("El modificador " + modificador.getNombre()
                    + " esta " + AccesibilidadUtil.ETIQUETA_AGOTADO);
        }
        if (coctel.esMocktail() && modificador.esAlcoholico()) {
            throw new ReglaNegocioException("El modificador " + modificador.getNombre()
                    + " tiene alcohol y no se puede agregar al Mocktail " + coctel.getNombre());
        }
    }

    /**
     * Flujo del KDS: RECIBIDO -> EN_PREPARACION -> LISTO -> ENTREGADO, sin saltos ni retrocesos.
     * Cancelar solo es posible desde RECIBIDO.
     */
    public void validarTransicion(EstadoPedido actual, EstadoPedido nuevo) {
        if (nuevo == EstadoPedido.CANCELADO) {
            if (actual != EstadoPedido.RECIBIDO) {
                throw new ReglaNegocioException(
                        "Solo se puede cancelar una comanda en estado RECIBIDO. Estado actual: " + actual);
            }
            return;
        }
        if (actual.siguiente() != nuevo) {
            throw new ReglaNegocioException(
                    "Transicion invalida de " + actual + " a " + nuevo);
        }
    }

    /**
     * Modificacion limitada: el licor se puede cambiar una sola vez y solo antes de que la
     * comanda pase a preparacion.
     */
    public void validarCambioDeLicor(Pedido pedido, ItemPedido item) {
        if (!pedido.puedeModificarse()) {
            throw new ReglaNegocioException(
                    "El licor solo se puede cambiar mientras la comanda esta en RECIBIDO");
        }
        if (item.getTipo() == TipoBebida.MOCKTAIL) {
            throw new ReglaNegocioException("Un Mocktail no tiene licor para cambiar");
        }
        int cambios = item.getCambiosDeLicor() == null ? 0 : item.getCambiosDeLicor();
        if (cambios >= MAX_CAMBIOS_DE_LICOR) {
            throw new ReglaNegocioException(
                    "El licor de este coctel ya fue cambiado una vez");
        }
    }
}
