package com.restaurante.repository;

import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.persistence.entity.CoctelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoctelRepository extends JpaRepository<CoctelEntity, Long> {

    List<CoctelEntity> findAllByOrderByIdAsc();

    List<CoctelEntity> findByCategoriaOrderByIdAsc(CategoriaCoctel categoria);

    List<CoctelEntity> findByDisponibleTrueOrderByIdAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
