package com.restaurante.repository;

import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.TipoBebida;
import com.restaurante.persistence.entity.CoctelEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CoctelRepositoryTest {

    @Autowired
    private CoctelRepository coctelRepository;

    private CoctelEntity negroni;

    private CoctelEntity coctel(String nombre, CategoriaCoctel categoria, boolean disponible) {
        CoctelEntity entity = new CoctelEntity();
        entity.setNombre(nombre);
        entity.setPrecio(38000.0);
        entity.setCategoria(categoria);
        entity.setTipo(TipoBebida.ALCOHOLICA);
        entity.setDestiladoBase("Gin");
        entity.setDisponible(disponible);
        return entity;
    }

    @BeforeEach
    void setUp() {
        negroni = coctelRepository.save(coctel("Negroni", CategoriaCoctel.CLASICO, true));
        coctelRepository.save(coctel("Pina Colada", CategoriaCoctel.TROPICAL, false));
    }

    @Test
    void guardarAsignaIdYFechaDeCreacion() {
        coctelRepository.flush();

        assertThat(negroni.getId()).isNotNull();
        assertThat(negroni.getCreadoEn()).isNotNull();
    }

    @Test
    void existeNombreSinImportarMayusculas() {
        assertThat(coctelRepository.existsByNombreIgnoreCase("NEGRONI")).isTrue();
        assertThat(coctelRepository.existsByNombreIgnoreCase("Martini")).isFalse();
        assertThat(coctelRepository.existsByNombreIgnoreCaseAndIdNot("negroni", negroni.getId())).isFalse();
    }

    @Test
    void consultasPorCategoriaYDisponibilidad() {
        assertThat(coctelRepository.findAllByOrderByIdAsc()).hasSize(2);
        assertThat(coctelRepository.findByCategoriaOrderByIdAsc(CategoriaCoctel.TROPICAL))
                .extracting(CoctelEntity::getNombre).containsExactly("Pina Colada");
        assertThat(coctelRepository.findByDisponibleTrueOrderByIdAsc())
                .extracting(CoctelEntity::getNombre).containsExactly("Negroni");
    }
}
