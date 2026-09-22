package com.restaurante.service;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.Coctel;
import com.restaurante.validator.CoctelValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementacion en memoria (sin base de datos) del servicio de cocteles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoctelServiceImpl implements CoctelService {

    private static final String RECURSO = "Coctel";

    private final CoctelValidator coctelValidator;

    private final Map<Long, Coctel> cocteles = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    @Override
    public List<Coctel> listar() {
        return cocteles.values().stream()
                .sorted(Comparator.comparing(Coctel::getId))
                .toList();
    }

    @Override
    public List<Coctel> listarPorCategoria(CategoriaCoctel categoria) {
        return listar().stream()
                .filter(c -> c.getCategoria() == categoria)
                .toList();
    }

    @Override
    public List<Coctel> obtenerDisponibles() {
        return listar().stream()
                .filter(Coctel::estaDisponible)
                .toList();
    }

    @Override
    public Coctel obtenerPorId(Long id) {
        Coctel coctel = cocteles.get(id);
        if (coctel == null) {
            log.warn("Coctel no encontrado: id={}", id);
            throw new RecursoNoEncontradoException(RECURSO, id);
        }
        return coctel;
    }

    @Override
    public Coctel crear(Coctel coctel) {
        coctelValidator.validarCoctel(coctel);
        coctelValidator.validarNombreUnico(coctel.getNombre(), cocteles.values(), null);
        coctel.setId(secuencia.incrementAndGet());
        if (coctel.getDisponible() == null) {
            coctel.setDisponible(true);
        }
        cocteles.put(coctel.getId(), coctel);
        log.info("Coctel creado: id={}, nombre={}", coctel.getId(), coctel.getNombre());
        return coctel;
    }

    @Override
    public Coctel actualizar(Long id, Coctel coctel) {
        Coctel existente = obtenerPorId(id);
        coctelValidator.validarCoctel(coctel);
        coctelValidator.validarNombreUnico(coctel.getNombre(), cocteles.values(), id);
        coctel.setId(id);
        if (coctel.getDisponible() == null) {
            coctel.setDisponible(existente.getDisponible());
        }
        cocteles.put(id, coctel);
        log.info("Coctel actualizado: id={}", id);
        return coctel;
    }

    @Override
    public Coctel cambiarDisponibilidad(Long id, boolean disponible) {
        Coctel coctel = obtenerPorId(id);
        coctel.setDisponible(disponible);
        log.info("Coctel id={} disponible={}", id, disponible);
        return coctel;
    }

    @Override
    public void eliminar(Long id) {
        obtenerPorId(id);
        cocteles.remove(id);
        log.info("Coctel eliminado: id={}", id);
    }
}
