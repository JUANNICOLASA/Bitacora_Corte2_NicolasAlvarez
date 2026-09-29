package com.restaurante.repository;

import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.persistence.entity.PedidoEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {

    @EntityGraph(attributePaths = "items")
    List<PedidoEntity> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "items")
    List<PedidoEntity> findByEstadoOrderByIdAsc(EstadoPedido estado);

    @EntityGraph(attributePaths = "items")
    List<PedidoEntity> findByEstadoNotInOrderByFechaCreacionAsc(Collection<EstadoPedido> estados);

    @Query("select p from PedidoEntity p left join fetch p.items where p.id = :id")
    Optional<PedidoEntity> buscarConItems(@Param("id") Long id);

    boolean existsByItemsCoctelId(Long idCoctel);
}
