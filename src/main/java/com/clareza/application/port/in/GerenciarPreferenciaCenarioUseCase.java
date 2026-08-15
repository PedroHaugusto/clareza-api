package com.clareza.application.port.in;

import com.clareza.domain.model.PreferenciaCenario;

import java.math.BigDecimal;

public interface GerenciarPreferenciaCenarioUseCase {

    PreferenciaCenario consultar(Long usuarioId);

    PreferenciaCenario salvar(Long usuarioId, BigDecimal ajusteReceita, BigDecimal ajusteDespesa);
}
