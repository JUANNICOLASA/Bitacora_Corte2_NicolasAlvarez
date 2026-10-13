package com.restaurante.service;

import com.restaurante.exception.RecursoEnUsoException;
import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.CoctelEntityMapper;
import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.Coctel;
import com.restaurante.persistence.entity.CoctelEntity;
import com.restaurante.repository.CoctelRepository;
import com.restaurante.repository.PedidoRepository;
import com.restaurante.validator.CoctelValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// S07 - Service = "la cocina": tiene la logica de negocio, valida las reglas del dominio y llama al Repository.
// No conoce HTTP (aqui no hay ResponseEntity ni codigos de estado).
// S08 - @Slf4j (Lombok) crea la variable log. INFO = flujo normal, WARN = algo raro, ERROR = fallo, DEBUG = detalle.
// S09 - @Transactional: cada metodo es una transaccion ACID (Atomicidad, Consistencia, Aislamiento, Durabilidad):
// o se hace todo o se revierte todo. readOnly = true en las consultas.
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CoctelServiceImpl implements CoctelService {

    private static final String RECURSO = "Coctel";

    // Inyeccion por constructor (IoC): repository, mapper OUT (dominio <-> entidad) y validator
    private final CoctelRepository coctelRepository;
    private final PedidoRepository pedidoRepository;
    private final CoctelEntityMapper coctelEntityMapper;
    private final CoctelValidator coctelValidator;

    @Override
    @Transactional(readOnly = true)
    public List<Coctel> listar() {
        // S08 - Streams: cada entidad se transforma a dominio con map y se recoge con toList()
        return coctelRepository.findAllByOrderByIdAsc().stream()
                .map(coctelEntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Coctel> listarPorCategoria(CategoriaCoctel categoria) {
        return coctelRepository.findByCategoriaOrderByIdAsc(categoria).stream()
                .map(coctelEntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Coctel> obtenerDisponibles() {
        return coctelRepository.findByDisponibleTrueOrderByIdAsc().stream()
                .map(coctelEntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Coctel obtenerPorId(Long id) {
        return coctelEntityMapper.toDomain(buscarEntidad(id));
    }

    @Override
    public Coctel crear(Coctel coctel) {
        // Validaciones de NEGOCIO en el service (las de input ya se hicieron en el DTO). Primero input, luego negocio
        coctelValidator.validarCoctel(coctel);
        coctelValidator.validarNombreUnico(coctel.getNombre(), null);
        coctel.setId(null);
        coctel.setNombre(coctel.getNombre().trim());
        if (coctel.getDisponible() == null) {
            coctel.setDisponible(true);
        }
        // dominio -> entidad -> save (la BD asigna el id) -> entidad -> dominio
        CoctelEntity guardado = coctelRepository.save(coctelEntityMapper.toEntity(coctel));
        log.info("Coctel creado: id={}, nombre={}", guardado.getId(), guardado.getNombre());
        return coctelEntityMapper.toDomain(guardado);
    }

    @Override
    public Coctel actualizar(Long id, Coctel coctel) {
        CoctelEntity entity = buscarEntidad(id);
        coctelValidator.validarCoctel(coctel);
        coctelValidator.validarNombreUnico(coctel.getNombre(), id);
        coctel.setNombre(coctel.getNombre().trim());
        if (coctel.getDisponible() == null) {
            coctel.setDisponible(entity.getDisponible());
        }
        coctelEntityMapper.actualizarEntity(coctel, entity);
        CoctelEntity guardado = coctelRepository.save(entity);
        log.info("Coctel actualizado: id={}", id);
        return coctelEntityMapper.toDomain(guardado);
    }

    @Override
    public Coctel cambiarDisponibilidad(Long id, boolean disponible) {
        CoctelEntity entity = buscarEntidad(id);
        entity.setDisponible(disponible);
        CoctelEntity guardado = coctelRepository.save(entity);
        log.info("Coctel id={} disponible={}", id, disponible);
        return coctelEntityMapper.toDomain(guardado);
    }

    @Override
    public void eliminar(Long id) {
        buscarEntidad(id);
        // Regla de negocio: no se elimina un coctel con comandas, se lanza excepcion propia -> 409
        if (pedidoRepository.existsByItemsCoctelId(id)) {
            throw new RecursoEnUsoException(
                    "El coctel tiene comandas registradas. Marquelo como agotado en lugar de eliminarlo");
        }
        coctelRepository.deleteById(id);
        log.info("Coctel eliminado: id={}", id);
    }

    private CoctelEntity buscarEntidad(Long id) {
        // No existe -> RecursoNoEncontradoException -> el GlobalExceptionHandler responde 404
        return coctelRepository.findById(id).orElseThrow(() -> {
            log.warn("Coctel no encontrado: id={}", id);
            return new RecursoNoEncontradoException(RECURSO, id);
        });
    }
}
