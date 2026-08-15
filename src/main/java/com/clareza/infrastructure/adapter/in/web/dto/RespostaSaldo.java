package com.clareza.infrastructure.adapter.in.web.dto;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class RespostaSaldo {

    BigDecimal saldoDisponivel;
    BigDecimal saldoRealizado;
}
