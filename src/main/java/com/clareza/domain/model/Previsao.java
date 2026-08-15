package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import lombok.Value;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Value
public class Previsao {

    Cenario cenario;
    BigDecimal percentualAjusteReceita;
    BigDecimal percentualAjusteDespesa;
    List<PrevisaoMensal> meses;

    private Previsao(Cenario cenario, PreferenciaCenario preferencia, List<PrevisaoMensal> meses) {
        this.cenario = cenario;
        this.percentualAjusteReceita = preferencia.getPercentualAjusteReceita();
        this.percentualAjusteDespesa = preferencia.getPercentualAjusteDespesa();
        this.meses = Collections.unmodifiableList(meses);
    }

    public static Previsao montar(YearMonth primeiraCompetencia, int quantidadeDeMeses,
                                  BigDecimal saldoInicial, List<Transacao> transacoes,
                                  Cenario cenario, PreferenciaCenario preferencia) {
        if (quantidadeDeMeses != 6 && quantidadeDeMeses != 12) {
            throw new RegraDeNegocioException("O horizonte da previsao deve ser de 6 ou 12 meses");
        }

        List<PrevisaoMensal> meses = new ArrayList<>(quantidadeDeMeses);
        BigDecimal saldoDoMesAnterior = saldoInicial;

        for (int passo = 0; passo < quantidadeDeMeses; passo++) {
            YearMonth competencia = primeiraCompetencia.plusMonths(passo);
            PrevisaoMensal mes = new PrevisaoMensal(
                    competencia, saldoDoMesAnterior, doMes(transacoes, competencia),
                    cenario, preferencia);

            meses.add(mes);
            saldoDoMesAnterior = mes.getSaldoProjetado();
        }

        return new Previsao(cenario, preferencia, meses);
    }

    private static List<Transacao> doMes(List<Transacao> transacoes, YearMonth competencia) {
        return transacoes.stream()
                .filter(transacao -> YearMonth.from(transacao.getDataPrevista()).equals(competencia))
                .collect(Collectors.toList());
    }
}
