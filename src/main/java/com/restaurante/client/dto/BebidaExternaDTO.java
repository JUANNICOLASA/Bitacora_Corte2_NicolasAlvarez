package com.restaurante.client.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.restaurante.util.TextoUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class BebidaExternaDTO {

    private static final int MAX_INGREDIENTES = 15;

    @JsonProperty("strDrink")
    private String nombre;

    @JsonProperty("strGlass")
    private String vaso;

    @JsonProperty("strInstructions")
    private String instrucciones;

    @JsonProperty("strInstructionsES")
    private String instruccionesEs;

    @JsonProperty("strDrinkThumb")
    private String imagen;

    private final Map<String, String> otrosCampos = new HashMap<>();

    @JsonAnySetter
    public void agregarCampo(String clave, Object valor) {
        if (valor != null) {
            otrosCampos.put(clave, valor.toString());
        }
    }

    public List<String> ingredientes() {
        List<String> ingredientes = new ArrayList<>();
        for (int i = 1; i <= MAX_INGREDIENTES; i++) {
            String ingrediente = otrosCampos.get("strIngredient" + i);
            if (TextoUtil.estaVacio(ingrediente)) {
                continue;
            }
            String medida = otrosCampos.get("strMeasure" + i);
            ingredientes.add(TextoUtil.estaVacio(medida)
                    ? ingrediente.trim()
                    : medida.trim() + " " + ingrediente.trim());
        }
        return ingredientes;
    }
}
