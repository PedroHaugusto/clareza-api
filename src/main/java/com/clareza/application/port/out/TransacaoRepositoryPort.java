package com.clareza.application.port.out;

import com.clareza.domain.model.Transacao;

import java.util.List;
import java.util.Optional;

public interface TransacaoRepositoryPort {

    List<Transacao> listarDoUsuario(Long usuarioId);

    Optional<Transacao> buscarPorId(Long id);

    Transacao salvar(Transacao transacao);

    void excluir(Long id);

    boolean existeComConta(Long contaId);

    boolean existeComCategoria(Long categoriaId);
}
