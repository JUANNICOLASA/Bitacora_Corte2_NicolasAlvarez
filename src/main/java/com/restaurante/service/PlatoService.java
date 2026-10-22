package com.restaurante.service;

import com.restaurante.model.domain.CategoriaPlato;
import com.restaurante.model.domain.Plato;

import java.util.List;

public interface PlatoService {

    List<Plato> listar();

    List<Plato> listarPorCategoria(CategoriaPlato categoria);

    Plato obtenerPorId(Long id);

    Plato crear(Plato plato);

    Plato actualizar(Long id, Plato plato);

    Plato cambiarDisponibilidad(Long id, boolean disponible);

    void eliminar(Long id);
}
