package com.restaurante.service;

import com.restaurante.model.domain.Resena;
import com.restaurante.model.domain.ResumenCalificacion;
import com.restaurante.model.domain.TipoProducto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ResenaService {

    List<Resena> listar(TipoProducto tipo, Long idProducto);

    List<ResumenCalificacion> resumen();

    Resena crear(Resena resena, Authentication usuario);
}
