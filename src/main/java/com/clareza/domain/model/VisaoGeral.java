package com.clareza.domain.model;

import lombok.Value;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Value
public class VisaoGeral {

    BigDecimal saldoDisponivel;
    BigDecimal saldoRealizado;
    ResumoDoMes mesAtual;
    List<ResumoDoMes> proximosMeses;

    private VisaoGeral(BigDecimal saldoDisponivel, BigDecimal saldoRealizado,
                       ResumoDoMes mesAtual, List<ResumoDoMes> proximosMeses) {
        this.saldoDisponivel = saldoDisponivel;
        this.saldoRealizado = saldoRealizado;
        this.mesAtual = mesAtual;
        this.proximosMeses = Collections.unmodifiableList(proximosMeses);
    }

    public static VisaoGeral montar(List<TotalMensal> totais, YearMonth competenciaAtual,
                                    int quantidadeDeMesesFuturos) {
        List<TotalMensal> ateOFimDoMesAtual = totais.stream()
                .filter(total -> !total.getCompetencia().isAfter(competenciaAtual))
                .collect(Collectors.toList());

        List<ResumoDoMes> proximos = new ArrayList<>(quantidadeDeMesesFuturos);
        for (int passo = 1; passo <= quantidadeDeMesesFuturos; passo++) {
            YearMonth competencia = competenciaAtual.plusMonths(passo);
            proximos.add(new ResumoDoMes(competencia, doMes(totais, competencia)));
        }

        return new VisaoGeral(
                saldo(ateOFimDoMesAtual, false),
                saldo(ateOFimDoMesAtual, true),
                new ResumoDoMes(competenciaAtual, doMes(totais, competenciaAtual)),
                proximos);
    }

    private static List<TotalMensal> doMes(List<TotalMensal> totais, YearMonth competencia) {
        return totais.stream()
                .filter(total -> total.getCompetencia().equals(competencia))
                .collect(Collectors.toList());
    }

    private static BigDecimal saldo(List<TotalMensal> totais, boolean somenteConfirmados) {
        BigDecimal receitas = BigDecimal.ZERO;
        BigDecimal despesas = BigDecimal.ZERO;

        for (TotalMensal total : totais) {
            if (somenteConfirmados && total.getStatus() != StatusTransacao.CONFIRMADA) {
                continue;
            }
            if (total.getTipo() == TipoTransacao.RECEITA) {
                receitas = receitas.add(total.getTotal());
            } else {
                despesas = despesas.add(total.getTotal());
            }
        }

        return receitas.subtract(despesas);
    }
}
