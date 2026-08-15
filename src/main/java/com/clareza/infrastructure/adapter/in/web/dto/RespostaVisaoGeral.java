package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.domain.model.ResumoDoMes;
import com.clareza.domain.model.VisaoGeral;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Value
@Builder
public class RespostaVisaoGeral {

    BigDecimal saldoDisponivel;
    BigDecimal saldoRealizado;
    RespostaResumoDoMes mesAtual;
    List<RespostaResumoDoMes> proximosMeses;

    public static RespostaVisaoGeral de(VisaoGeral visaoGeral) {
        return RespostaVisaoGeral.builder()
                .saldoDisponivel(visaoGeral.getSaldoDisponivel())
                .saldoRealizado(visaoGeral.getSaldoRealizado())
                .mesAtual(RespostaResumoDoMes.de(visaoGeral.getMesAtual()))
                .proximosMeses(visaoGeral.getProximosMeses().stream()
                        .map(RespostaResumoDoMes::de)
                        .collect(Collectors.toList()))
                .build();
    }

    @Value
    @Builder
    public static class RespostaResumoDoMes {

        int mes;
        int ano;
        BigDecimal receitasRealizadas;
        BigDecimal receitasPrevistas;
        BigDecimal despesasRealizadas;
        BigDecimal despesasPrevistas;
        BigDecimal totalReceitas;
        BigDecimal totalDespesas;
        BigDecimal saldoDoMes;

        static RespostaResumoDoMes de(ResumoDoMes resumo) {
            return RespostaResumoDoMes.builder()
                    .mes(resumo.getMes())
                    .ano(resumo.getAno())
                    .receitasRealizadas(resumo.getReceitasRealizadas())
                    .receitasPrevistas(resumo.getReceitasPrevistas())
                    .despesasRealizadas(resumo.getDespesasRealizadas())
                    .despesasPrevistas(resumo.getDespesasPrevistas())
                    .totalReceitas(resumo.getTotalReceitas())
                    .totalDespesas(resumo.getTotalDespesas())
                    .saldoDoMes(resumo.getSaldoDoMes())
                    .build();
        }
    }
}
