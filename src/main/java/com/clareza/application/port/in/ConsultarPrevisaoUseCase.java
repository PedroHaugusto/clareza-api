package com.clareza.application.port.in;

import com.clareza.domain.model.Previsao;

public interface ConsultarPrevisaoUseCase {

    Previsao consultar(ComandoDePrevisao comando);
}
