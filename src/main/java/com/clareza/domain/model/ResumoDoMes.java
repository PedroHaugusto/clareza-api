package com.clareza.domain.model;

import lombok.Value;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Value
public class ResumoDoMes {

    int mes;
    int ano;
    BigDecimal receitasRealizadas;
    BigDecimal receitasPrevistas;
    BigDecimal despesasRealizadas;
    BigDecimal despesasPrevistas;

    public ResumoDoMes(YearMonth competencia, List<TotalMensal> totais) {
        this.mes = competencia.getMonthValue();
        this.ano = competencia.getYear();
        this.receitasRealizadas = somar(totais, TipoTransacao.RECEITA, StatusTransacao.CONFIRMADA);
        this.receitasPrevistas = somar(totais, TipoTransacao.RECEITA, StatusTransacao.PREVISTA);
        this.despesasRealizadas = somar(totais, TipoTransacao.DESPESA, StatusTransacao.CONFIRMADA);
        this.despesasPrevistas = somar(totais, TipoTransacao.DESPESA, StatusTransacao.PREVISTA);
    }

    public BigDecimal getTotalReceitas() {
        return receitasRealizadas.add(receitasPrevistas);
    }

    public BigDecimal getTotalDespesas() {
        return despesasRealizadas.add(despesasPrevistas);
    }

    public BigDecimal getSaldoDoMes() {
        return getTotalReceitas().subtract(getTotalDespesas());
    }

    private static BigDecimal somar(List<TotalMensal> totais, TipoTransacao tipo, StatusTransacao status) {
        return totais.stream()
                .filter(total -> total.ehDe(tipo, status))
                .map(TotalMensal::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
