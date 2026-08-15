package com.clareza.application.port.out;

import com.clareza.domain.model.Conta;

import java.util.List;
import java.util.Optional;

public interface ContaRepositoryPort {

    List<Conta> listarDoUsuario(Long usuarioId);

    Optional<Conta> buscarPorId(Long id);

    Conta salvar(Conta conta);

    void excluir(Long id);

    boolean existeComNomeDoUsuario(String nome, Long usuarioId);
}
