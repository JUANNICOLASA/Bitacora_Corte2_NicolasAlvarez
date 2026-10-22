package com.restaurante.service;

import com.restaurante.client.CoctelesExternosClient;
import com.restaurante.client.dto.BebidaExternaDTO;
import com.restaurante.client.dto.RespuestaCocteleraDTO;
import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.exception.ServicioExternoException;
import com.restaurante.mapper.RecetaExternaMapper;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.RecetaExterna;
import com.restaurante.util.TextoUtil;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecetaExternaServiceImpl implements RecetaExternaService {

    private final CoctelService coctelService;
    private final CoctelesExternosClient coctelesExternosClient;
    private final RecetaExternaMapper recetaExternaMapper;

    @Override
    public RecetaExterna buscarPorCoctel(Long idCoctel) {
        Coctel coctel = coctelService.obtenerPorId(idCoctel);
        RespuestaCocteleraDTO respuesta = consultar(coctel.getNombre());
        List<BebidaExternaDTO> bebidas = respuesta == null || respuesta.drinks() == null
                ? List.of()
                : respuesta.drinks();
        return bebidas.stream()
                .filter(bebida -> TextoUtil.sonIguales(bebida.getNombre(), coctel.getNombre()))
                .findFirst()
                .map(recetaExternaMapper::toDomain)
                .orElseThrow(() -> {
                    log.info("No hay receta de referencia para el coctel {}", coctel.getNombre());
                    return new RecursoNoEncontradoException("Receta de referencia del coctel", idCoctel);
                });
    }

    private RespuestaCocteleraDTO consultar(String nombre) {
        try {
            return coctelesExternosClient.buscarPorNombre(TextoUtil.normalizar(nombre));
        } catch (FeignException ex) {
            log.error("Fallo la consulta a TheCocktailDB para {}: {}", nombre, ex.getMessage());
            throw new ServicioExternoException("El servicio de recetas no esta disponible en este momento");
        }
    }
}
