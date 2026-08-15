package com.clareza.application.port.out;

import com.clareza.domain.model.Usuario;

import java.util.Optional;

public interface UsuarioRepositoryPort {

    Usuario salvar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorEmail(String email);

    Optional<Usuario> buscarPorGoogleId(String googleId);

    boolean existePorEmail(String email);
}