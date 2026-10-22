package com.restaurante.service;

import com.restaurante.exception.RecursoDuplicadoException;
import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.ResenaDocumentMapper;
import com.restaurante.model.domain.Resena;
import com.restaurante.model.domain.ResumenCalificacion;
import com.restaurante.model.domain.Rol;
import com.restaurante.model.domain.TipoProducto;
import com.restaurante.model.domain.Usuario;
import com.restaurante.persistence.document.ResenaDocument;
import com.restaurante.repository.ResenaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResenaServiceImplTest {

    private static final String CORREO = "laura@correo.com";

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private CoctelService coctelService;

    @Mock
    private PlatoService platoService;

    @Mock
    private AuthService authService;

    @Spy
    private ResenaDocumentMapper resenaDocumentMapper = Mappers.getMapper(ResenaDocumentMapper.class);

    @InjectMocks
    private ResenaServiceImpl resenaService;

    private final Authentication cliente = new UsernamePasswordAuthenticationToken(CORREO, null,
            List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));

    private ResenaDocument documento(TipoProducto tipo, Long idProducto, int estrellas) {
        ResenaDocument documento = new ResenaDocument();
        documento.setId("r" + idProducto + estrellas);
        documento.setTipo(tipo);
        documento.setIdProducto(idProducto);
        documento.setAutor("Laura");
        documento.setEstrellas(estrellas);
        documento.setFecha(LocalDateTime.now());
        return documento;
    }

    private Resena solicitud(TipoProducto tipo, String comentario) {
        return Resena.builder().tipo(tipo).idProducto(1L).estrellas(5).comentario(comentario).build();
    }

    @Test
    void listarConvierteLosDocumentos() {
        when(resenaRepository.findByTipoAndIdProductoOrderByFechaDesc(TipoProducto.COCTEL, 1L))
                .thenReturn(List.of(documento(TipoProducto.COCTEL, 1L, 4)));

        assertThat(resenaService.listar(TipoProducto.COCTEL, 1L)).extracting(Resena::getEstrellas).containsExactly(4);
    }

    @Test
    void resumenAgrupaPorProductoYCalculaElPromedio() {
        when(resenaRepository.findAll()).thenReturn(List.of(
                documento(TipoProducto.PLATO, 2L, 3),
                documento(TipoProducto.COCTEL, 1L, 5),
                documento(TipoProducto.COCTEL, 1L, 4),
                documento(TipoProducto.COCTEL, 1L, 4)));

        List<ResumenCalificacion> resumen = resenaService.resumen();

        assertThat(resumen).hasSize(2);
        ResumenCalificacion coctel = resumen.get(0);
        assertThat(coctel.getTipo()).isEqualTo(TipoProducto.COCTEL);
        assertThat(coctel.getPromedio()).isEqualTo(4.3);
        assertThat(coctel.getTotal()).isEqualTo(3L);
        assertThat(coctel.getDistribucion()).containsEntry(4, 2L).containsEntry(5, 1L).containsEntry(1, 0L);
        assertThat(resumen.get(1).getTipo()).isEqualTo(TipoProducto.PLATO);
    }

    @Test
    void resumenSinOpinionesEsVacio() {
        when(resenaRepository.findAll()).thenReturn(List.of());

        assertThat(resenaService.resumen()).isEmpty();
    }

    @Test
    void crearUsaElNombreDelUsuarioYLimpiaElComentario() {
        when(authService.obtenerPorEmail(CORREO))
                .thenReturn(Usuario.builder().nombre("Laura Gomez").email(CORREO).rol(Rol.CLIENTE).build());
        when(resenaRepository.save(any(ResenaDocument.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Resena creada = resenaService.crear(solicitud(TipoProducto.COCTEL, "  Muy bueno  "), cliente);

        ArgumentCaptor<ResenaDocument> guardada = ArgumentCaptor.forClass(ResenaDocument.class);
        verify(resenaRepository).save(guardada.capture());
        assertThat(guardada.getValue().getUsuario()).isEqualTo(CORREO);
        assertThat(creada.getAutor()).isEqualTo("Laura Gomez");
        assertThat(creada.getComentario()).isEqualTo("Muy bueno");
        assertThat(creada.getFecha()).isNotNull();
        verify(coctelService).obtenerPorId(1L);
    }

    @Test
    void crearSobreUnPlatoSinComentario() {
        when(authService.obtenerPorEmail(CORREO))
                .thenReturn(Usuario.builder().nombre("Laura").email(CORREO).rol(Rol.CLIENTE).build());
        when(resenaRepository.save(any(ResenaDocument.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Resena creada = resenaService.crear(solicitud(TipoProducto.PLATO, "   "), cliente);

        assertThat(creada.getComentario()).isNull();
        verify(platoService).obtenerPorId(1L);
    }

    @Test
    void crearSobreProductoInexistenteNoGuarda() {
        when(coctelService.obtenerPorId(1L)).thenThrow(new RecursoNoEncontradoException("Coctel", 1L));

        assertThatThrownBy(() -> resenaService.crear(solicitud(TipoProducto.COCTEL, null), cliente))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(resenaRepository, never()).save(any());
    }

    @Test
    void unClienteSoloCalificaUnaVezCadaProducto() {
        when(resenaRepository.existsByTipoAndIdProductoAndUsuario(TipoProducto.COCTEL, 1L, CORREO)).thenReturn(true);

        assertThatThrownBy(() -> resenaService.crear(solicitud(TipoProducto.COCTEL, null), cliente))
                .isInstanceOf(RecursoDuplicadoException.class);
        verify(resenaRepository, never()).save(any());
    }
}
