package com.restaurante.service;

import com.restaurante.exception.RecursoDuplicadoException;
import com.restaurante.mapper.ResenaDocumentMapper;
import com.restaurante.model.domain.Resena;
import com.restaurante.model.domain.ResumenCalificacion;
import com.restaurante.model.domain.TipoProducto;
import com.restaurante.persistence.document.ResenaDocument;
import com.restaurante.repository.ResenaRepository;
import com.restaurante.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResenaServiceImpl implements ResenaService {

    private final ResenaRepository resenaRepository;
    private final ResenaDocumentMapper resenaDocumentMapper;
    private final CoctelService coctelService;
    private final PlatoService platoService;
    private final AuthService authService;

    private record Producto(TipoProducto tipo, Long idProducto) {
    }

    @Override
    public List<Resena> listar(TipoProducto tipo, Long idProducto) {
        return resenaRepository.findByTipoAndIdProductoOrderByFechaDesc(tipo, idProducto).stream()
                .map(resenaDocumentMapper::toDomain)
                .toList();
    }

    @Override
    public List<ResumenCalificacion> resumen() {
        Map<Producto, List<ResenaDocument>> porProducto = resenaRepository.findAll().stream()
                .collect(Collectors.groupingBy(resena -> new Producto(resena.getTipo(), resena.getIdProducto())));
        return porProducto.entrySet().stream()
                .map(entrada -> resumir(entrada.getKey(), entrada.getValue()))
                .sorted(Comparator.comparing(ResumenCalificacion::getTipo)
                        .thenComparing(ResumenCalificacion::getIdProducto))
                .toList();
    }

    private ResumenCalificacion resumir(Producto producto, List<ResenaDocument> resenas) {
        Map<Integer, Long> distribucion = new TreeMap<>();
        IntStream.rangeClosed(1, 5).forEach(estrella -> distribucion.put(estrella, 0L));
        resenas.forEach(resena -> distribucion.merge(resena.getEstrellas(), 1L, Long::sum));
        double promedio = resenas.stream().mapToInt(ResenaDocument::getEstrellas).average().orElse(0);
        return ResumenCalificacion.builder()
                .tipo(producto.tipo())
                .idProducto(producto.idProducto())
                .promedio(Math.round(promedio * 10) / 10.0)
                .total((long) resenas.size())
                .distribucion(distribucion)
                .build();
    }

    @Override
    public Resena crear(Resena resena, Authentication usuario) {
        validarProductoExiste(resena.getTipo(), resena.getIdProducto());
        String correo = usuario.getName();
        if (resenaRepository.existsByTipoAndIdProductoAndUsuario(resena.getTipo(), resena.getIdProducto(), correo)) {
            throw new RecursoDuplicadoException("Ya calificaste este producto");
        }
        resena.setId(null);
        resena.setUsuario(correo);
        resena.setAutor(authService.obtenerPorEmail(correo).getNombre());
        resena.setComentario(TextoUtil.estaVacio(resena.getComentario()) ? null : resena.getComentario().trim());
        resena.setFecha(LocalDateTime.now());
        ResenaDocument guardada = resenaRepository.save(resenaDocumentMapper.toDocument(resena));
        log.info("Resena registrada: {} id={} con {} estrellas", resena.getTipo(), resena.getIdProducto(),
                resena.getEstrellas());
        return resenaDocumentMapper.toDomain(guardada);
    }

    private void validarProductoExiste(TipoProducto tipo, Long idProducto) {
        if (tipo == TipoProducto.COCTEL) {
            coctelService.obtenerPorId(idProducto);
        } else {
            platoService.obtenerPorId(idProducto);
        }
    }
}
