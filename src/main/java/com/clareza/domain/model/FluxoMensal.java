package com.clareza.domain.model;

import lombok.Value;

import java.math.BigDecimal;
import java.time.YearMonth;

@Value
public class FluxoMensal {

    int mes;
    int ano;
    BigDecimal entradas;
    BigDecimal saidas;
    BigDecimal saldoDoMes;
    BigDecimal saldoAcumulado;

    FluxoMensal(YearMonth competencia, BigDecimal entradas, BigDecimal saidas,
                BigDecimal saldoAcumuladoAnterior) {
        this.mes = competencia.getMonthValue();
        this.ano = competencia.getYear();
        this.entradas = entradas;
        this.saidas = saidas;
        this.saldoDoMes = entradas.subtract(saidas);
        this.saldoAcumulado = saldoAcumuladoAnterior.add(this.saldoDoMes);
    }
}
