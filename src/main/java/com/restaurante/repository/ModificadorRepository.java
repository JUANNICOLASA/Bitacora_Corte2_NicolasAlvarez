package com.restaurante.repository;

import com.restaurante.persistence.entity.ModificadorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModificadorRepository extends JpaRepository<ModificadorEntity, Long> {

    List<ModificadorEntity> findAllByOrderByIdAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
