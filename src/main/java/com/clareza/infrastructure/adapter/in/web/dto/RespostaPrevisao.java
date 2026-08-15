package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.domain.model.Cenario;
import com.clareza.domain.model.Previsao;
import com.clareza.domain.model.PrevisaoMensal;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Value
@Builder
public class RespostaPrevisao {

    Cenario cenario;
    BigDecimal percentualAjusteReceita;
    BigDecimal percentualAjusteDespesa;
    List<RespostaPrevisaoMensal> meses;

    public static RespostaPrevisao de(Previsao previsao, LocalDate hoje) {
        return RespostaPrevisao.builder()
                .cenario(previsao.getCenario())
                .percentualAjusteReceita(previsao.getPercentualAjusteReceita())
                .percentualAjusteDespesa(previsao.getPercentualAjusteDespesa())
                .meses(previsao.getMeses().stream()
                        .map(mes -> RespostaPrevisaoMensal.de(mes, hoje))
                        .collect(Collectors.toList()))
                .build();
    }

    @Value
    @Builder
    public static class RespostaPrevisaoMensal {

        int mes;
        int ano;
        BigDecimal saldoInicial;
        BigDecimal totalReceitasPrevistas;
        BigDecimal totalDespesasPrevistas;
        BigDecimal saldoProjetado;
        List<RespostaTransacao> transacoes;

        static RespostaPrevisaoMensal de(PrevisaoMensal mes, LocalDate hoje) {
            return RespostaPrevisaoMensal.builder()
                    .mes(mes.getMes())
                    .ano(mes.getAno())
                    .saldoInicial(mes.getSaldoInicial())
                    .totalReceitasPrevistas(mes.getTotalReceitasPrevistas())
                    .totalDespesasPrevistas(mes.getTotalDespesasPrevistas())
                    .saldoProjetado(mes.getSaldoProjetado())
                    .transacoes(mes.getTransacoes().stream()
                            .map(transacao -> RespostaTransacao.de(transacao, hoje))
                            .collect(Collectors.toList()))
                    .build();
        }
    }
}
