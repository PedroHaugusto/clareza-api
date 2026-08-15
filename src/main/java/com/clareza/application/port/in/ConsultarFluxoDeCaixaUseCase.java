package com.clareza.application.port.in;

import com.clareza.domain.model.FluxoDeCaixa;

public interface ConsultarFluxoDeCaixaUseCase {

    FluxoDeCaixa consultar(Long usuarioId, int mesesPassados, int mesesFuturos);
}
