package com.restaurante.persistence.entity;

import com.restaurante.model.domain.CategoriaCoctel;
import com.restaurante.model.domain.TipoBebida;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// S09 - Entidad JPA: representa la TABLA coctel en PostgreSQL. Solo persistencia, sin logica de negocio
// (la logica esta en model/domain/Coctel). Si se mezclan, un cambio de esquema afecta el negocio.
// Indices en categoria y disponible porque la carta filtra por esas columnas. Un indice acelera las
// lecturas pero ocupa espacio y se tiene que actualizar en cada insert/update/delete.
@Entity
@Table(name = "coctel", indexes = {
        @Index(name = "idx_coctel_categoria", columnList = "categoria"),
        @Index(name = "idx_coctel_disponible", columnList = "disponible")
})
@Getter
@Setter
@NoArgsConstructor
public class CoctelEntity {

    @Id
    // S09 - El id lo asigna la BD (IDENTITY) y queda disponible despues del save().
    // Long: tabla interna con muchas FK y rapida en joins (no se usa UUID porque no es un id publico sensible)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(length = 250)
    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    // Enum guardado como texto y no como numero, asi no se dana si se reordena el enum
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaCoctel categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private TipoBebida tipo;

    @Column(name = "destilado_base", length = 40)
    private String destiladoBase;

    @Column(nullable = false)
    private Boolean disponible;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;
}
