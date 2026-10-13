package com.restaurante.service;

import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.Coctel;

import java.util.List;

// S07 - Inversion de dependencias: el contrato del service es una interfaz y la implementacion va en
// CoctelServiceImpl. El controller depende de la abstraccion -> bajo acoplamiento y alta cohesion.
// S08 - Los metodos reciben y devuelven objetos de DOMINIO, nunca DTOs.
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
