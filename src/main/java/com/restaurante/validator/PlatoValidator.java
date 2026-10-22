package com.restaurante.validator;

import com.restaurante.exception.RecursoDuplicadoException;
import com.restaurante.repository.PlatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlatoValidator {

    private final PlatoRepository platoRepository;

    public void validarNombreUnico(String nombre, Long idExcluido) {
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        boolean duplicado = idExcluido == null
                ? platoRepository.existsByNombreIgnoreCase(nombreLimpio)
                : platoRepository.existsByNombreIgnoreCaseAndIdNot(nombreLimpio, idExcluido);
        if (duplicado) {
            throw new RecursoDuplicadoException("Ya existe un plato con el nombre " + nombreLimpio);
        }
    }
}
