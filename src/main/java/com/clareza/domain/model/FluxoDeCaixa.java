package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import lombok.Value;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Value
public class FluxoDeCaixa {

    BigDecimal saldoAnterior;
    List<FluxoMensal> meses;

    private FluxoDeCaixa(BigDecimal saldoAnterior, List<FluxoMensal> meses) {
        this.saldoAnterior = saldoAnterior;
        this.meses = Collections.unmodifiableList(meses);
    }

    public static FluxoDeCaixa montar(List<TotalMensal> totais,
                                      YearMonth primeiraCompetencia,
                                      YearMonth ultimaCompetencia) {
        if (ultimaCompetencia.isBefore(primeiraCompetencia)) {
            throw new RegraDeNegocioException("O fim do periodo nao pode ser anterior ao inicio");
        }

        BigDecimal saldoAnterior = saldoAntesDe(totais, primeiraCompetencia);
        BigDecimal acumulado = saldoAnterior;

        List<FluxoMensal> meses = new ArrayList<>();
        YearMonth competencia = primeiraCompetencia;

        while (!competencia.isAfter(ultimaCompetencia)) {
            FluxoMensal mes = new FluxoMensal(
                    competencia,
                    somar(totais, competencia, TipoTransacao.RECEITA),
                    somar(totais, competencia, TipoTransacao.DESPESA),
                    acumulado);

            meses.add(mes);
            acumulado = mes.getSaldoAcumulado();
            competencia = competencia.plusMonths(1);
        }

        return new FluxoDeCaixa(saldoAnterior, meses);
    }

    private static BigDecimal saldoAntesDe(List<TotalMensal> totais, YearMonth competencia) {
        BigDecimal saldo = BigDecimal.ZERO;
        for (TotalMensal total : totais) {
            if (!total.getCompetencia().isBefore(competencia)) {
                continue;
            }
            saldo = total.getTipo() == TipoTransacao.RECEITA
                    ? saldo.add(total.getTotal())
                    : saldo.subtract(total.getTotal());
        }
        return saldo;
    }

    private static BigDecimal somar(List<TotalMensal> totais, YearMonth competencia, TipoTransacao tipo) {
        return totais.stream()
                .filter(total -> total.getCompetencia().equals(competencia))
                .filter(total -> total.getTipo() == tipo)
                .map(TotalMensal::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
