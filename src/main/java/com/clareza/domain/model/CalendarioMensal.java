package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Value
public class CalendarioMensal {

    int mes;
    int ano;
    BigDecimal totalReceitas;
    BigDecimal totalDespesas;
    List<DiaDoCalendario> dias;

    private CalendarioMensal(int mes, int ano, List<DiaDoCalendario> dias) {
        this.mes = mes;
        this.ano = ano;
        this.dias = Collections.unmodifiableList(dias);
        this.totalReceitas = somar(dias, true);
        this.totalDespesas = somar(dias, false);
    }

    public static CalendarioMensal montar(int mes, int ano, List<Transacao> transacoes) {
        if (mes < 1 || mes > 12) {
            throw new RegraDeNegocioException("O mes deve estar entre 1 e 12");
        }

        YearMonth referencia = YearMonth.of(ano, mes);
        Map<LocalDate, List<Transacao>> porDia = new TreeMap<>();

        for (Transacao transacao : transacoes) {
            LocalDate data = transacao.getDataPrevista();
            if (!YearMonth.from(data).equals(referencia)) {
                continue;
            }
            porDia.computeIfAbsent(data, chave -> new ArrayList<>()).add(transacao);
        }

        List<DiaDoCalendario> dias = new ArrayList<>(porDia.size());
        for (Map.Entry<LocalDate, List<Transacao>> entrada : porDia.entrySet()) {
            dias.add(new DiaDoCalendario(entrada.getKey(), entrada.getValue()));
        }

        return new CalendarioMensal(mes, ano, dias);
    }

    public BigDecimal getSaldoDoMes() {
        return totalReceitas.subtract(totalDespesas);
    }

    private static BigDecimal somar(List<DiaDoCalendario> dias, boolean receitas) {
        return dias.stream()
                .map(dia -> receitas ? dia.getTotalReceitas() : dia.getTotalDespesas())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
