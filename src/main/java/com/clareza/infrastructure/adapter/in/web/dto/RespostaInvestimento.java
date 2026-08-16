package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.domain.model.Investimento;
import com.clareza.domain.model.TipoInvestimento;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class RespostaInvestimento {

    Long id;
    String nome;
    TipoInvestimento tipo;
    BigDecimal valorInvestido;
    BigDecimal rentabilidadeInformada;

    public static RespostaInvestimento de(Investimento investimento) {
        return RespostaInvestimento.builder()
                .id(investimento.getId())
                .nome(investimento.getNome())
                .tipo(investimento.getTipo())
                .valorInvestido(investimento.getValorInvestido())
                .rentabilidadeInformada(investimento.getRentabilidadeInformada())
                .build();
    }
}
