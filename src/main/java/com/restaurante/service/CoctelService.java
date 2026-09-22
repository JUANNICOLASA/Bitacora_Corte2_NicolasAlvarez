package com.restaurante.service;

import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.Coctel;

import java.util.List;

/**
 * Contrato del servicio de cocteles. Lo usan CoctelController (administracion)
 * y MenuController (vista del cliente).
 */
public interface CoctelService {

    List<Coctel> listar();

    List<Coctel> listarPorCategoria(CategoriaCoctel categoria);

    List<Coctel> obtenerDisponibles();

    Coctel obtenerPorId(Long id);

    Coctel crear(Coctel coctel);

    Coctel actualizar(Long id, Coctel coctel);

    Coctel cambiarDisponibilidad(Long id, boolean disponible);

    void eliminar(Long id);
}
