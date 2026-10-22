package com.restaurante.client;

import com.restaurante.client.dto.RespuestaCocteleraDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "coctelesExternos", url = "${cocteles-externos.url}")
public interface CoctelesExternosClient {

    @GetMapping("/search.php")
    RespuestaCocteleraDTO buscarPorNombre(@RequestParam("s") String nombre);
}
