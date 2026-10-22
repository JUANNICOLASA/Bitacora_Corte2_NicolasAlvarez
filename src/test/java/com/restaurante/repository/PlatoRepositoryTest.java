package com.restaurante.repository;

import com.restaurante.model.domain.CategoriaPlato;
import com.restaurante.persistence.entity.PlatoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PlatoRepositoryTest {

    @Autowired
    private PlatoRepository platoRepository;

    private PlatoEntity papas;

    private PlatoEntity plato(String nombre, CategoriaPlato categoria) {
        PlatoEntity entity = new PlatoEntity();
        entity.setNombre(nombre);
        entity.setPrecio(22000.0);
        entity.setCategoria(categoria);
        entity.setDisponible(true);
        return entity;
    }

    @BeforeEach
    void setUp() {
        papas = platoRepository.save(plato("Papas trufadas", CategoriaPlato.PARA_COMPARTIR));
        platoRepository.save(plato("Volcan de chocolate", CategoriaPlato.POSTRES));
    }

    @Test
    void filtraPorCategoriaYValidaNombresSinMayusculas() {
        assertThat(platoRepository.findByCategoriaOrderByIdAsc(CategoriaPlato.POSTRES))
                .extracting(PlatoEntity::getNombre).containsExactly("Volcan de chocolate");
        assertThat(platoRepository.findAllByOrderByIdAsc()).hasSize(2);
        assertThat(platoRepository.existsByNombreIgnoreCase("PAPAS TRUFADAS")).isTrue();
        assertThat(platoRepository.existsByNombreIgnoreCaseAndIdNot("papas trufadas", papas.getId())).isFalse();
    }
}
