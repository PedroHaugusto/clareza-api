package com.clareza.application.port.in;

import com.clareza.domain.model.VisaoGeral;

public interface ConsultarVisaoGeralUseCase {

    VisaoGeral consultar(Long usuarioId);
}
