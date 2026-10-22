package com.restaurante.repository;

import com.restaurante.model.domain.CategoriaPlato;
import com.restaurante.persistence.entity.PlatoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlatoRepository extends JpaRepository<PlatoEntity, Long> {

    List<PlatoEntity> findAllByOrderByIdAsc();

    List<PlatoEntity> findByCategoriaOrderByIdAsc(CategoriaPlato categoria);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
