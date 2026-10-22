package com.restaurante.repository;

import com.restaurante.persistence.entity.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservaRepository extends JpaRepository<ReservaEntity, UUID> {

    List<ReservaEntity> findByFechaOrderByHoraAsc(LocalDate fecha);

    List<ReservaEntity> findByFechaGreaterThanEqualOrderByFechaAscHoraAsc(LocalDate fecha);

    List<ReservaEntity> findByUsuarioOrderByFechaDescHoraDesc(String usuario);

    boolean existsByCodigo(String codigo);
}
