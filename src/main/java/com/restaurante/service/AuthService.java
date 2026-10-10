package com.restaurante.service;

import com.restaurante.model.domain.Usuario;

public interface AuthService {

    String login(String email, String password);

    String loginOAuth2(String email, String nombre);

    Usuario registrar(Usuario usuario);

    Usuario crear(Usuario usuario);

    boolean existe(String email);

    Usuario obtenerPorEmail(String email);
}
