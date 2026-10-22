package com.restaurante.service;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.ReservaEntityMapper;
import com.restaurante.model.domain.EstadoReserva;
import com.restaurante.model.domain.Reserva;
import com.restaurante.persistence.entity.ReservaEntity;
import com.restaurante.repository.ReservaRepository;
import com.restaurante.util.CodigoUtil;
import com.restaurante.util.TextoUtil;
import com.restaurante.validator.ReservaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private static final String RECURSO = "Reserva";

    private final ReservaRepository reservaRepository;
    private final ReservaEntityMapper reservaEntityMapper;
    private final ReservaValidator reservaValidator;
    private final Clock reloj;

    @Override
    public Reserva crear(Reserva reserva, Authentication usuario) {
        reservaValidator.validarFecha(reserva.getFecha());
        reservaValidator.validarHora(reserva.getHora());
        reserva.setId(null);
        reserva.setCodigo(generarCodigoUnico());
        reserva.setNombre(reserva.getNombre().trim());
        reserva.setTelefono(reserva.getTelefono().trim());
        reserva.setCorreo(reserva.getCorreo().trim().toLowerCase());
        reserva.setOcasion(TextoUtil.estaVacio(reserva.getOcasion()) ? null : reserva.getOcasion().trim());
        reserva.setNotas(TextoUtil.estaVacio(reserva.getNotas()) ? null : reserva.getNotas().trim());
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setUsuario(usuario == null ? null : usuario.getName());
        reserva.setFechaCreacion(LocalDateTime.now(reloj));
        ReservaEntity guardada = reservaRepository.save(reservaEntityMapper.toEntity(reserva));
        log.info("Reserva {} creada para el {} a las {} ({} personas)", guardada.getCodigo(), guardada.getFecha(),
                guardada.getHora(), guardada.getPersonas());
        return reservaEntityMapper.toDomain(guardada);
    }

    private String generarCodigoUnico() {
        String codigo = CodigoUtil.generarCodigoReserva();
        while (reservaRepository.existsByCodigo(codigo)) {
            codigo = CodigoUtil.generarCodigoReserva();
        }
        return codigo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> listar(LocalDate fecha) {
        List<ReservaEntity> reservas = fecha == null
                ? reservaRepository.findByFechaGreaterThanEqualOrderByFechaAscHoraAsc(LocalDate.now(reloj))
                : reservaRepository.findByFechaOrderByHoraAsc(fecha);
        return reservas.stream()
                .map(reservaEntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> listarMias(Authentication usuario) {
        return reservaRepository.findByUsuarioOrderByFechaDescHoraDesc(usuario.getName()).stream()
                .map(reservaEntityMapper::toDomain)
                .toList();
    }

    @Override
    public Reserva cambiarEstado(UUID id, EstadoReserva nuevoEstado) {
        ReservaEntity entity = buscarEntidad(id);
        reservaValidator.validarCambioEstado(entity.getEstado(), nuevoEstado);
        log.info("Reserva {} cambia de {} a {}", entity.getCodigo(), entity.getEstado(), nuevoEstado);
        entity.setEstado(nuevoEstado);
        return reservaEntityMapper.toDomain(reservaRepository.save(entity));
    }

    @Override
    public void cancelar(UUID id, Authentication usuario) {
        ReservaEntity entity = buscarEntidad(id);
        reservaValidator.validarPropietario(reservaEntityMapper.toDomain(entity), usuario);
        reservaValidator.validarCambioEstado(entity.getEstado(), EstadoReserva.CANCELADA);
        entity.setEstado(EstadoReserva.CANCELADA);
        reservaRepository.save(entity);
        log.info("Reserva {} cancelada por {}", entity.getCodigo(), usuario.getName());
    }

    private ReservaEntity buscarEntidad(UUID id) {
        return reservaRepository.findById(id).orElseThrow(() -> {
            log.warn("Reserva no encontrada: id={}", id);
            return new RecursoNoEncontradoException(RECURSO, id);
        });
    }
}
