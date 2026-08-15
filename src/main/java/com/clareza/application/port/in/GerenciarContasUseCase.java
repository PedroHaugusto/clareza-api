package com.clareza.application.port.in;

import com.clareza.domain.model.Conta;

import java.util.List;

public interface GerenciarContasUseCase {

    List<Conta> listar(Long usuarioId);

    Conta criar(ComandoDeCriacaoDeConta comando);

    void excluir(Long contaId, Long usuarioId);
}
