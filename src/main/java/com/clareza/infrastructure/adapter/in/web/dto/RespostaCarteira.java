package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.domain.model.Carteira;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Value
@Builder
public class RespostaCarteira {

    BigDecimal totalInvestido;
    BigDecimal rentabilidadeMediaPonderada;
    int quantidade;
    List<RespostaInvestimento> investimentos;

    public static RespostaCarteira de(Carteira carteira) {
        return RespostaCarteira.builder()
                .totalInvestido(carteira.getTotalInvestido())
                .rentabilidadeMediaPonderada(carteira.getRentabilidadeMediaPonderada())
                .quantidade(carteira.getQuantidade())
                .investimentos(carteira.getInvestimentos().stream()
                        .map(RespostaInvestimento::de)
                        .collect(Collectors.toList()))
                .build();
    }
}
