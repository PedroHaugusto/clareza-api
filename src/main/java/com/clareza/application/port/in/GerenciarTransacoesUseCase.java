package com.clareza.application.port.in;

import com.clareza.domain.model.Transacao;

import java.util.List;

public interface GerenciarTransacoesUseCase {

    List<Transacao> listar(FiltroDeTransacoes filtro);

    Transacao confirmar(Long transacaoId, Long usuarioId);

    Transacao criar(ComandoDeTransacao comando);

    Transacao editar(Long transacaoId, ComandoDeTransacao comando);

    void excluir(Long transacaoId, Long usuarioId);
}
