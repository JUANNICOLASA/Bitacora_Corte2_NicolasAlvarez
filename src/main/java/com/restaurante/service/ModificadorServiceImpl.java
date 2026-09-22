package com.restaurante.service;

import com.restaurante.exception.RecursoDuplicadoException;
import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.model.domain.Modificador;
import com.restaurante.util.TextoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementacion en memoria (sin base de datos) del servicio de modificadores.
 */
@Slf4j
@Service
public class ModificadorServiceImpl implements ModificadorService {

    private static final String RECURSO = "Modificador";

    private final Map<Long, Modificador> modificadores = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    @Override
    public List<Modificador> listar() {
        return modificadores.values().stream()
                .sorted(Comparator.comparing(Modificador::getId))
                .toList();
    }

    @Override
    public Modificador obtenerPorId(Long id) {
        Modificador modificador = modificadores.get(id);
        if (modificador == null) {
            log.warn("Modificador no encontrado: id={}", id);
            throw new RecursoNoEncontradoException(RECURSO, id);
        }
        return modificador;
    }

    @Override
    public Modificador crear(Modificador modificador) {
        boolean duplicado = modificadores.values().stream()
                .anyMatch(m -> TextoUtil.sonIguales(m.getNombre(), modificador.getNombre()));
        if (duplicado) {
            throw new RecursoDuplicadoException(
                    "Ya existe un modificador con el nombre " + modificador.getNombre());
        }
        modificador.setId(secuencia.incrementAndGet());
        if (modificador.getDisponible() == null) {
            modificador.setDisponible(true);
        }
        modificadores.put(modificador.getId(), modificador);
        log.info("Modificador creado: id={}, nombre={}", modificador.getId(), modificador.getNombre());
        return modificador;
    }

    @Override
    public Modificador cambiarDisponibilidad(Long id, boolean disponible) {
        Modificador modificador = obtenerPorId(id);
        modificador.setDisponible(disponible);
        log.info("Modificador id={} disponible={}", id, disponible);
        return modificador;
    }
}
