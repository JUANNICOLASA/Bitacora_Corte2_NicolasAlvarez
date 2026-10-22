package com.restaurante.service;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.PlatoEntityMapper;
import com.restaurante.model.domain.CategoriaPlato;
import com.restaurante.model.domain.Plato;
import com.restaurante.persistence.entity.PlatoEntity;
import com.restaurante.repository.PlatoRepository;
import com.restaurante.validator.PlatoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PlatoServiceImpl implements PlatoService {

    private static final String RECURSO = "Plato";

    private final PlatoRepository platoRepository;
    private final PlatoEntityMapper platoEntityMapper;
    private final PlatoValidator platoValidator;

    @Override
    @Transactional(readOnly = true)
    public List<Plato> listar() {
        return platoRepository.findAllByOrderByIdAsc().stream()
                .map(platoEntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plato> listarPorCategoria(CategoriaPlato categoria) {
        return platoRepository.findByCategoriaOrderByIdAsc(categoria).stream()
                .map(platoEntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Plato obtenerPorId(Long id) {
        return platoEntityMapper.toDomain(buscarEntidad(id));
    }

    @Override
    public Plato crear(Plato plato) {
        platoValidator.validarNombreUnico(plato.getNombre(), null);
        plato.setId(null);
        plato.setNombre(plato.getNombre().trim());
        if (plato.getDisponible() == null) {
            plato.setDisponible(true);
        }
        PlatoEntity guardado = platoRepository.save(platoEntityMapper.toEntity(plato));
        log.info("Plato creado: id={}, nombre={}", guardado.getId(), guardado.getNombre());
        return platoEntityMapper.toDomain(guardado);
    }

    @Override
    public Plato actualizar(Long id, Plato plato) {
        PlatoEntity entity = buscarEntidad(id);
        platoValidator.validarNombreUnico(plato.getNombre(), id);
        plato.setNombre(plato.getNombre().trim());
        if (plato.getDisponible() == null) {
            plato.setDisponible(entity.getDisponible());
        }
        platoEntityMapper.actualizarEntity(plato, entity);
        PlatoEntity guardado = platoRepository.save(entity);
        log.info("Plato actualizado: id={}", id);
        return platoEntityMapper.toDomain(guardado);
    }

    @Override
    public Plato cambiarDisponibilidad(Long id, boolean disponible) {
        PlatoEntity entity = buscarEntidad(id);
        entity.setDisponible(disponible);
        PlatoEntity guardado = platoRepository.save(entity);
        log.info("Plato id={} disponible={}", id, disponible);
        return platoEntityMapper.toDomain(guardado);
    }

    @Override
    public void eliminar(Long id) {
        buscarEntidad(id);
        platoRepository.deleteById(id);
        log.info("Plato eliminado: id={}", id);
    }

    private PlatoEntity buscarEntidad(Long id) {
        return platoRepository.findById(id).orElseThrow(() -> {
            log.warn("Plato no encontrado: id={}", id);
            return new RecursoNoEncontradoException(RECURSO, id);
        });
    }
}
