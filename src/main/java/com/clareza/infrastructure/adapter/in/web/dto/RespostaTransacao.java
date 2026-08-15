package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.domain.model.StatusTransacao;
import com.clareza.domain.model.TipoTransacao;
import com.clareza.domain.model.Transacao;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Value
@Builder
public class RespostaTransacao {

    Long id;
    Long contaId;
    Long categoriaId;
    String descricao;
    BigDecimal valor;
    TipoTransacao tipo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dataPrevista;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dataEfetivacao;

    StatusTransacao status;
    UUID grupoParcelamentoId;
    Integer numeroParcela;
    Integer totalParcelas;

    public static RespostaTransacao de(Transacao transacao, LocalDate hoje) {
        return RespostaTransacao.builder()
                .id(transacao.getId())
                .contaId(transacao.getContaId())
                .categoriaId(transacao.getCategoriaId())
                .descricao(transacao.getDescricao())
                .valor(transacao.getValor())
                .tipo(transacao.getTipo())
                .dataPrevista(transacao.getDataPrevista())
                .dataEfetivacao(transacao.getDataEfetivacao())
                .status(transacao.statusEm(hoje))
                .grupoParcelamentoId(transacao.getGrupoParcelamentoId())
                .numeroParcela(transacao.getNumeroParcela())
                .totalParcelas(transacao.getTotalParcelas())
                .build();
    }
}
