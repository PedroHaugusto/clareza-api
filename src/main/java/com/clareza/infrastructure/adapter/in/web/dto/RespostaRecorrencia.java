package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.domain.model.Periodicidade;
import com.clareza.domain.model.TipoTransacao;
import com.clareza.domain.model.TransacaoRecorrente;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class RespostaRecorrencia {

    Long id;
    Long contaId;
    Long categoriaId;
    String descricao;
    BigDecimal valor;
    TipoTransacao tipo;
    Periodicidade periodicidade;
    Integer diaDoMes;
    Integer diaDaSemana;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dataInicio;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dataFim;

    boolean ativa;

    public static RespostaRecorrencia de(TransacaoRecorrente recorrente) {
        return RespostaRecorrencia.builder()
                .id(recorrente.getId())
                .contaId(recorrente.getContaId())
                .categoriaId(recorrente.getCategoriaId())
                .descricao(recorrente.getDescricao())
                .valor(recorrente.getValor())
                .tipo(recorrente.getTipo())
                .periodicidade(recorrente.getPeriodicidade())
                .diaDoMes(recorrente.getDiaDoMes())
                .diaDaSemana(recorrente.getDiaDaSemana())
                .dataInicio(recorrente.getDataInicio())
                .dataFim(recorrente.getDataFim())
                .ativa(recorrente.isAtiva())
                .build();
    }
}
