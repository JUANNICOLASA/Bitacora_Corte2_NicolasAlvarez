package com.restaurante.repository;

import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.persistence.entity.CoctelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// S07/S09 - Repository = "la despensa": solo accede a los datos, no tiene logica propia.
// Interfaz que extiende JpaRepository<Entidad, TipoDelId>: ya trae save, findById, findAll, deleteById,
// existsById y count sin escribir SQL. 1 repositorio por cada entidad.
// Los demas metodos se generan solos por la convencion de nombres de Spring Data (query methods).
@Repository
public interface CoctelRepository extends JpaRepository<CoctelEntity, Long> {

    List<CoctelEntity> findAllByOrderByIdAsc();

    List<CoctelEntity> findByCategoriaOrderByIdAsc(CategoriaCoctel categoria);

    List<CoctelEntity> findByDisponibleTrueOrderByIdAsc();

    // Lo usa el validator: la BD busca el duplicado (columna unique con indice) en vez de cargar todo a memoria
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
