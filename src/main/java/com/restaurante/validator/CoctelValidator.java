package com.restaurante.validator;

import com.restaurante.exception.RecursoDuplicadoException;
import com.restaurante.exception.ReglaNegocioException;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.util.TextoUtil;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;

/**
 * Reglas de negocio de la carta de cocteles.
 */
@Component
public class CoctelValidator {

    /**
     * Trazabilidad del alcohol en la carta: un coctel alcoholico debe declarar su destilado base
     * y un Mocktail no puede tener destilado.
     */
    public void validarCoctel(Coctel coctel) {
        boolean sinDestilado = TextoUtil.estaVacio(coctel.getDestiladoBase());
        if (coctel.getTipo() == TipoBebida.ALCOHOLICA && sinDestilado) {
            throw new ReglaNegocioException(
                    "Un coctel alcoholico debe especificar su destilado base");
        }
        if (coctel.getTipo() == TipoBebida.MOCKTAIL && !sinDestilado) {
            throw new ReglaNegocioException(
                    "Un Mocktail no puede tener destilado base");
        }
    }

    /**
     * No pueden existir dos cocteles con el mismo nombre.
     *
     * @param idExcluido id del coctel que se esta actualizando (null al crear)
     */
    public void validarNombreUnico(String nombre, Collection<Coctel> existentes, Long idExcluido) {
        boolean duplicado = existentes.stream()
                .filter(c -> !Objects.equals(c.getId(), idExcluido))
                .anyMatch(c -> TextoUtil.sonIguales(c.getNombre(), nombre));
        if (duplicado) {
            throw new RecursoDuplicadoException("Ya existe un coctel con el nombre " + nombre);
        }
    }
}
