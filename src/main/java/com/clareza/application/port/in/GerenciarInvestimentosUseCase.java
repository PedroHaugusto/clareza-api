package com.clareza.application.port.in;

import com.clareza.domain.model.Carteira;
import com.clareza.domain.model.Investimento;

public interface GerenciarInvestimentosUseCase {

    Carteira consultarCarteira(Long usuarioId);

    Investimento criar(ComandoDeInvestimento comando);

    Investimento editar(Long investimentoId, ComandoDeInvestimento comando);

    void excluir(Long investimentoId, Long usuarioId);
}
