package com.clareza.application.port.in;

import com.clareza.domain.model.Transacao;

import java.util.List;

public interface CriarTransacaoParceladaUseCase {

    List<Transacao> parcelar(ComandoDeParcelamento comando);
}
