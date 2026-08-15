package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.domain.model.FluxoDeCaixa;
import com.clareza.domain.model.FluxoMensal;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Value
@Builder
public class RespostaFluxoDeCaixa {

    BigDecimal saldoAnterior;
    List<RespostaFluxoMensal> meses;

    public static RespostaFluxoDeCaixa de(FluxoDeCaixa fluxo) {
        return RespostaFluxoDeCaixa.builder()
                .saldoAnterior(fluxo.getSaldoAnterior())
                .meses(fluxo.getMeses().stream()
                        .map(RespostaFluxoMensal::de)
                        .collect(Collectors.toList()))
                .build();
    }

    @Value
    @Builder
    public static class RespostaFluxoMensal {

        int mes;
        int ano;
        BigDecimal entradas;
        BigDecimal saidas;
        BigDecimal saldoDoMes;
        BigDecimal saldoAcumulado;

        static RespostaFluxoMensal de(FluxoMensal mes) {
            return RespostaFluxoMensal.builder()
                    .mes(mes.getMes())
                    .ano(mes.getAno())
                    .entradas(mes.getEntradas())
                    .saidas(mes.getSaidas())
                    .saldoDoMes(mes.getSaldoDoMes())
                    .saldoAcumulado(mes.getSaldoAcumulado())
                    .build();
        }
    }
}
