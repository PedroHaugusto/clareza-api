package com.clareza.application.port.in;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class ComandoDeMetaFinanceira {

    Long usuarioId;
    String nome;
    BigDecimal valorAtual;
    BigDecimal valorObjetivo;
    LocalDate prazo;
    String descricao;
}
