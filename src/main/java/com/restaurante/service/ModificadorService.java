package com.restaurante.service;

import com.restaurante.model.domain.Modificador;

import java.util.List;

/**
 * Contrato del servicio de modificadores (adiciones de los cocteles).
 */
public interface ModificadorService {

    List<Modificador> listar();

    Modificador obtenerPorId(Long id);

    Modificador crear(Modificador modificador);

    Modificador cambiarDisponibilidad(Long id, boolean disponible);
}
