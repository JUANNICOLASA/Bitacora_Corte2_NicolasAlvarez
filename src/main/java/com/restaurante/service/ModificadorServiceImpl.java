package com.restaurante.service;

import com.restaurante.exception.RecursoDuplicadoException;
import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.ModificadorEntityMapper;
import com.restaurante.model.domain.Modificador;
import com.restaurante.persistence.entity.ModificadorEntity;
import com.restaurante.repository.ModificadorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ModificadorServiceImpl implements ModificadorService {

    private static final String RECURSO = "Modificador";

    private final ModificadorRepository modificadorRepository;
    private final ModificadorEntityMapper modificadorEntityMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Modificador> listar() {
        return modificadorRepository.findAllByOrderByIdAsc().stream()
                .map(modificadorEntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Modificador obtenerPorId(Long id) {
        return modificadorEntityMapper.toDomain(buscarEntidad(id));
    }

    @Override
    public Modificador crear(Modificador modificador) {
        String nombre = modificador.getNombre().trim();
        if (modificadorRepository.existsByNombreIgnoreCase(nombre)) {
            throw new RecursoDuplicadoException("Ya existe un modificador con el nombre " + nombre);
        }
        modificador.setId(null);
        modificador.setNombre(nombre);
        if (modificador.getDisponible() == null) {
            modificador.setDisponible(true);
        }
        ModificadorEntity guardado = modificadorRepository.save(modificadorEntityMapper.toEntity(modificador));
        log.info("Modificador creado: id={}, nombre={}", guardado.getId(), guardado.getNombre());
        return modificadorEntityMapper.toDomain(guardado);
    }

    @Override
    public Modificador cambiarDisponibilidad(Long id, boolean disponible) {
        ModificadorEntity entity = buscarEntidad(id);
        entity.setDisponible(disponible);
        ModificadorEntity guardado = modificadorRepository.save(entity);
        log.info("Modificador id={} disponible={}", id, disponible);
        return modificadorEntityMapper.toDomain(guardado);
    }

    private ModificadorEntity buscarEntidad(Long id) {
        return modificadorRepository.findById(id).orElseThrow(() -> {
            log.warn("Modificador no encontrado: id={}", id);
            return new RecursoNoEncontradoException(RECURSO, id);
        });
    }
}
