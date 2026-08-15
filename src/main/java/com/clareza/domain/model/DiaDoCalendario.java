package com.clareza.domain.model;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Value
public class DiaDoCalendario {

    LocalDate data;
    BigDecimal totalReceitas;
    BigDecimal totalDespesas;
    List<Transacao> transacoes;

    public DiaDoCalendario(LocalDate data, List<Transacao> transacoes) {
        this.data = data;
        this.transacoes = Collections.unmodifiableList(transacoes);
        this.totalReceitas = somar(transacoes, TipoTransacao.RECEITA);
        this.totalDespesas = somar(transacoes, TipoTransacao.DESPESA);
    }

    public BigDecimal getSaldoDoDia() {
        return totalReceitas.subtract(totalDespesas);
    }

    private static BigDecimal somar(List<Transacao> transacoes, TipoTransacao tipo) {
        return transacoes.stream()
                .filter(transacao -> transacao.getTipo() == tipo)
                .map(Transacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
