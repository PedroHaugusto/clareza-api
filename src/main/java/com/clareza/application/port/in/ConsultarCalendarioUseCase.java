package com.clareza.application.port.in;

import com.clareza.domain.model.CalendarioMensal;

public interface ConsultarCalendarioUseCase {

    CalendarioMensal consultar(Long usuarioId, int mes, int ano);
}
