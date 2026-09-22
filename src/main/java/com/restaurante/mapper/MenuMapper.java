package com.restaurante.mapper;

import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.Modificador;
import com.restaurante.model.dto.response.MenuItemResponseDTO;
import com.restaurante.model.dto.response.ModificadorMenuResponseDTO;
import com.restaurante.util.AccesibilidadUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Construye la vista del cliente (carta digital). Ademas del estado, agrega etiqueta e icono
 * para no depender solo del color (regla de accesibilidad de Blue Velvet).
 */
@Mapper(componentModel = "spring", imports = AccesibilidadUtil.class)
public interface MenuMapper {

    @Mapping(target = "etiqueta", expression = "java(AccesibilidadUtil.etiquetaDisponibilidad(coctel.estaDisponible()))")
    @Mapping(target = "icono", expression = "java(AccesibilidadUtil.iconoDisponibilidad(coctel.estaDisponible()))")
    @Mapping(target = "seleccionable", expression = "java(coctel.estaDisponible())")
    MenuItemResponseDTO toMenuItem(Coctel coctel);

    List<MenuItemResponseDTO> toMenuItems(List<Coctel> cocteles);

    /**
     * Modificador visto dentro de un coctel: se bloquea si esta agotado o si tiene alcohol
     * y el coctel es Mocktail.
     */
    default ModificadorMenuResponseDTO toModificadorMenu(Modificador modificador, Coctel coctel) {
        boolean habilitado = modificador.esCompatibleCon(coctel);
        boolean bloqueadoPorMocktail = coctel.esMocktail() && modificador.esAlcoholico();
        return new ModificadorMenuResponseDTO(
                modificador.getId(),
                modificador.getNombre(),
                modificador.getGraduacionAlcoholica(),
                modificador.getPrecioExtra(),
                habilitado,
                AccesibilidadUtil.etiquetaModificador(modificador.estaDisponible(), bloqueadoPorMocktail),
                AccesibilidadUtil.iconoDisponibilidad(habilitado));
    }

    default List<ModificadorMenuResponseDTO> toModificadoresMenu(List<Modificador> modificadores, Coctel coctel) {
        return modificadores.stream()
                .map(modificador -> toModificadorMenu(modificador, coctel))
                .toList();
    }
}
