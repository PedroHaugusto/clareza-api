package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.domain.model.MetaFinanceira;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class RespostaMetaFinanceira {

    Long id;
    String nome;
    BigDecimal valorAtual;
    BigDecimal valorObjetivo;
    BigDecimal percentualConcluido;
    BigDecimal valorRestante;
    boolean concluida;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate prazo;

    Long diasAtePrazo;
    boolean prazoVencido;
    String descricao;

    public static RespostaMetaFinanceira de(MetaFinanceira meta, LocalDate hoje) {
        return RespostaMetaFinanceira.builder()
                .id(meta.getId())
                .nome(meta.getNome())
                .valorAtual(meta.getValorAtual())
                .valorObjetivo(meta.getValorObjetivo())
                .percentualConcluido(meta.getPercentualConcluido())
                .valorRestante(meta.getValorRestante())
                .concluida(meta.estaConcluida())
                .prazo(meta.getPrazo())
                .diasAtePrazo(meta.diasAte(hoje))
                .prazoVencido(meta.prazoVencidoEm(hoje))
                .descricao(meta.getDescricao())
                .build();
    }
}
