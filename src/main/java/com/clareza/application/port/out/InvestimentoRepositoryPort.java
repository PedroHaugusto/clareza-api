package com.clareza.application.port.out;

import com.clareza.domain.model.Investimento;

import java.util.List;
import java.util.Optional;

public interface InvestimentoRepositoryPort {

    List<Investimento> listarDoUsuario(Long usuarioId);

    Optional<Investimento> buscarPorId(Long id);

    Investimento salvar(Investimento investimento);

    void excluir(Long id);
}
