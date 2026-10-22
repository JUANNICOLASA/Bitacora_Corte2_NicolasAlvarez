package com.restaurante.service;

import com.restaurante.client.CoctelesExternosClient;
import com.restaurante.client.dto.BebidaExternaDTO;
import com.restaurante.client.dto.RespuestaCocteleraDTO;
import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.exception.ServicioExternoException;
import com.restaurante.mapper.RecetaExternaMapper;
import com.restaurante.model.domain.Coctel;
import com.restaurante.model.domain.RecetaExterna;
import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecetaExternaServiceImplTest {

    @Mock
    private CoctelService coctelService;

    @Mock
    private CoctelesExternosClient coctelesExternosClient;

    @Spy
    private RecetaExternaMapper recetaExternaMapper = Mappers.getMapper(RecetaExternaMapper.class);

    @InjectMocks
    private RecetaExternaServiceImpl recetaExternaService;

    @BeforeEach
    void setUp() {
        when(coctelService.obtenerPorId(5L)).thenReturn(Coctel.builder().id(5L).nombre("Piña Colada").build());
    }

    private BebidaExternaDTO bebida(String nombre, String instruccionesEs) {
        BebidaExternaDTO bebida = new BebidaExternaDTO();
        bebida.setNombre(nombre);
        bebida.setVaso("Collins glass");
        bebida.setInstrucciones("Mix with crushed ice.");
        bebida.setInstruccionesEs(instruccionesEs);
        bebida.setImagen("https://www.thecocktaildb.com/images/media/drink/pina.jpg");
        Map.of("strIngredient1", "Light rum", "strMeasure1", "3 oz ", "strIngredient2", "Coconut milk")
                .forEach(bebida::agregarCampo);
        return bebida;
    }

    @Test
    void devuelveLaRecetaQueCoincideConElNombre() {
        when(coctelesExternosClient.buscarPorNombre("pina colada")).thenReturn(new RespuestaCocteleraDTO(List.of(
                bebida("Pina Colada Royale", null), bebida("Pina Colada", "Mezclar con hielo picado."))));

        RecetaExterna receta = recetaExternaService.buscarPorCoctel(5L);

        assertThat(receta.getNombre()).isEqualTo("Pina Colada");
        assertThat(receta.getInstrucciones()).isEqualTo("Mezclar con hielo picado.");
        assertThat(receta.getIngredientes()).containsExactly("3 oz Light rum", "Coconut milk");
        assertThat(receta.getFuente()).isEqualTo("TheCocktailDB");
    }

    @Test
    void usaLasInstruccionesEnInglesSiNoHayEnEspanol() {
        when(coctelesExternosClient.buscarPorNombre("pina colada"))
                .thenReturn(new RespuestaCocteleraDTO(List.of(bebida("Pina Colada", " "))));

        assertThat(recetaExternaService.buscarPorCoctel(5L).getInstrucciones()).isEqualTo("Mix with crushed ice.");
    }

    @Test
    void sinResultadosLanzaNoEncontrado() {
        when(coctelesExternosClient.buscarPorNombre("pina colada")).thenReturn(new RespuestaCocteleraDTO(null));

        assertThatThrownBy(() -> recetaExternaService.buscarPorCoctel(5L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void respuestaVaciaLanzaNoEncontrado() {
        when(coctelesExternosClient.buscarPorNombre("pina colada")).thenReturn(null);

        assertThatThrownBy(() -> recetaExternaService.buscarPorCoctel(5L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void proveedorCaidoLanzaServicioExterno() {
        Request peticion = Request.create(Request.HttpMethod.GET, "/search.php", Map.of(), null,
                StandardCharsets.UTF_8, null);
        FeignException falla = new RetryableException(-1, "timeout", Request.HttpMethod.GET, (Long) null, peticion);
        when(coctelesExternosClient.buscarPorNombre("pina colada")).thenThrow(falla);

        assertThatThrownBy(() -> recetaExternaService.buscarPorCoctel(5L))
                .isInstanceOf(ServicioExternoException.class)
                .hasMessageContaining("no esta disponible");
    }
}
