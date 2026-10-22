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

    // S09 - Problema 1+N: 1 query para los pedidos + N queries para los items de cada uno.
    // @EntityGraph trae el pedido con sus items en una sola consulta.
    @EntityGraph(attributePaths = "items")
    List<PedidoEntity> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "items")
    List<PedidoEntity> findByEstadoOrderByIdAsc(EstadoPedido estado);

    @EntityGraph(attributePaths = "items")
    List<PedidoEntity> findByEstadoNotInOrderByFechaCreacionAsc(Collection<EstadoPedido> estados);

    @EntityGraph(attributePaths = "items")
    List<PedidoEntity> findByUsuarioOrderByIdDesc(String usuario);

    // @Query con JPQL escrito a mano (JOIN FETCH) cuando la consulta deja de ser trivial
    @Query("select p from PedidoEntity p left join fetch p.items where p.id = :id")
    Optional<PedidoEntity> buscarConItems(@Param("id") Long id);

    // Query method que navega la relacion pedido.items.coctel.id
    boolean existsByItemsCoctelId(Long idCoctel);
}
