package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.domain.model.PreferenciaCenario;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class RespostaPreferenciaCenario {

    BigDecimal percentualAjusteReceita;
    BigDecimal percentualAjusteDespesa;

    public static RespostaPreferenciaCenario de(PreferenciaCenario preferencia) {
        return new RespostaPreferenciaCenario(
                preferencia.getPercentualAjusteReceita(),
                preferencia.getPercentualAjusteDespesa());
    }
}
