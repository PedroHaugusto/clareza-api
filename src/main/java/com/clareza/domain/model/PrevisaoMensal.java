package com.clareza.domain.model;

import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

@Value
public class PrevisaoMensal {

    int mes;
    int ano;
    BigDecimal saldoInicial;
    BigDecimal totalReceitasPrevistas;
    BigDecimal totalDespesasPrevistas;
    BigDecimal saldoProjetado;
    List<Transacao> transacoes;

    PrevisaoMensal(YearMonth competencia, BigDecimal saldoInicial, List<Transacao> transacoes,
                   Cenario cenario, PreferenciaCenario preferencia) {
        this.mes = competencia.getMonthValue();
        this.ano = competencia.getYear();
        this.saldoInicial = saldoInicial;
        this.transacoes = Collections.unmodifiableList(transacoes);

        this.totalReceitasPrevistas = ajustar(
                somar(transacoes, TipoTransacao.RECEITA),
                cenario.fatorParaReceitas(preferencia.getPercentualAjusteReceita()));

        this.totalDespesasPrevistas = ajustar(
                somar(transacoes, TipoTransacao.DESPESA),
                cenario.fatorParaDespesas(preferencia.getPercentualAjusteDespesa()));

        this.saldoProjetado = saldoInicial
                .add(totalReceitasPrevistas)
                .subtract(totalDespesasPrevistas);
    }

    private static BigDecimal somar(List<Transacao> transacoes, TipoTransacao tipo) {
        return transacoes.stream()
                .filter(transacao -> transacao.getTipo() == tipo)
                .map(Transacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal ajustar(BigDecimal valor, BigDecimal fator) {
        return valor.multiply(fator).setScale(2, RoundingMode.HALF_UP);
    }
}
