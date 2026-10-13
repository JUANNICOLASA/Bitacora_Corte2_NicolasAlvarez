package com.restaurante.validator;

import com.restaurante.exception.RecursoDuplicadoException;
import com.restaurante.exception.ReglaNegocioException;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.repository.CoctelRepository;
import com.restaurante.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// S07/S08 - Validator: verifica reglas de NEGOCIO antes de procesar (las de formato van en el DTO con Bean Validation).
// Se saca del service para que cada clase tenga una sola responsabilidad. Es @Component porque se inyecta.
// S09 - Ya no recibe la lista completa en memoria: usa el repository y la BD hace la busqueda.
@Component
@RequiredArgsConstructor
public class CoctelValidator {

    private final CoctelRepository coctelRepository;

    public void validarCoctel(Coctel coctel) {
        boolean sinDestilado = TextoUtil.estaVacio(coctel.getDestiladoBase());
        if (coctel.getTipo() == TipoBebida.ALCOHOLICA && sinDestilado) {
            throw new ReglaNegocioException("Un coctel alcoholico debe especificar su destilado base");
        }
        if (coctel.getTipo() == TipoBebida.MOCKTAIL && !sinDestilado) {
            throw new ReglaNegocioException("Un Mocktail no puede tener destilado base");
        }
    }

    public void validarNombreUnico(String nombre, Long idExcluido) {
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        boolean duplicado = idExcluido == null
                ? coctelRepository.existsByNombreIgnoreCase(nombreLimpio)
                : coctelRepository.existsByNombreIgnoreCaseAndIdNot(nombreLimpio, idExcluido);
        if (duplicado) {
            throw new RecursoDuplicadoException("Ya existe un coctel con el nombre " + nombreLimpio);
        }
    }
}
