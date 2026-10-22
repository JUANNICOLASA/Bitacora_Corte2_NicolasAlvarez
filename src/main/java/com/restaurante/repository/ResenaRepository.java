package com.restaurante.repository;

import com.restaurante.model.domain.TipoProducto;
import com.restaurante.persistence.document.ResenaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResenaRepository extends MongoRepository<ResenaDocument, String> {

    List<ResenaDocument> findByTipoAndIdProductoOrderByFechaDesc(TipoProducto tipo, Long idProducto);

    boolean existsByTipoAndIdProductoAndUsuario(TipoProducto tipo, Long idProducto, String usuario);
}
