package com.clareza.domain.model;

import lombok.Value;

import java.math.BigDecimal;
import java.time.YearMonth;

@Value
public class TotalMensal {

    int ano;
    int mes;
    TipoTransacao tipo;
    StatusTransacao status;
    BigDecimal total;

    public YearMonth getCompetencia() {
        return YearMonth.of(ano, mes);
    }

    public boolean ehDe(TipoTransacao tipo, StatusTransacao status) {
        return this.tipo == tipo && this.status == status;
    }
}
