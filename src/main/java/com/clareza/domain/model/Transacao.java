package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@EqualsAndHashCode
public class Transacao {

    private final Long id;
    private final Long usuarioId;
    private final Long contaId;
    private final Long categoriaId;
    private final String descricao;
    private final BigDecimal valor;
    private final TipoTransacao tipo;
    private final LocalDate dataPrevista;
    private final LocalDate dataEfetivacao;
    private final StatusTransacao status;
    private final Long transacaoRecorrenteId;
    private final UUID grupoParcelamentoId;
    private final Integer numeroParcela;
    private final Integer totalParcelas;

    @Builder(toBuilder = true)
    private Transacao(Long id, Long usuarioId, Long contaId, Long categoriaId, String descricao,
                      BigDecimal valor, TipoTransacao tipo, LocalDate dataPrevista,
                      LocalDate dataEfetivacao, StatusTransacao status, Long transacaoRecorrenteId,
                      UUID grupoParcelamentoId, Integer numeroParcela, Integer totalParcelas) {
        exigir(usuarioId != null, "A transacao precisa pertencer a um usuario");
        exigir(contaId != null, "A conta e obrigatoria");
        exigir(categoriaId != null, "A categoria e obrigatoria");
        exigir(descricao != null && !descricao.trim().isEmpty(), "A descricao e obrigatoria");
        exigir(tipo != null, "O tipo e obrigatorio");
        exigir(dataPrevista != null, "A data prevista e obrigatoria");
        exigir(valor != null && valor.compareTo(BigDecimal.ZERO) > 0,
                "O valor deve ser positivo: o sinal vem do tipo da transacao");

        StatusTransacao statusResolvido = status == null ? StatusTransacao.PREVISTA : status;
        exigir(statusResolvido != StatusTransacao.ATRASADA,
                "ATRASADA e derivado da data na leitura, nunca gravado");

        if (dataEfetivacao == null) {
            exigir(statusResolvido == StatusTransacao.PREVISTA,
                    "Uma transacao confirmada precisa de data de efetivacao");
        } else {
            exigir(statusResolvido == StatusTransacao.CONFIRMADA,
                    "Somente transacao confirmada tem data de efetivacao");
        }

        this.id = id;
        this.usuarioId = usuarioId;
        this.contaId = contaId;
        this.categoriaId = categoriaId;
        this.descricao = descricao.trim();
        this.valor = valor;
        this.tipo = tipo;
        this.dataPrevista = dataPrevista;
        this.dataEfetivacao = dataEfetivacao;
        this.status = statusResolvido;
        this.transacaoRecorrenteId = transacaoRecorrenteId;
        this.grupoParcelamentoId = grupoParcelamentoId;
        this.numeroParcela = numeroParcela;
        this.totalParcelas = totalParcelas;
    }

    public StatusTransacao statusEm(LocalDate referencia) {
        if (status == StatusTransacao.PREVISTA && dataPrevista.isBefore(referencia)) {
            return StatusTransacao.ATRASADA;
        }
        return status;
    }

    public boolean estaConfirmada() {
        return status == StatusTransacao.CONFIRMADA;
    }

    public Transacao confirmarEm(LocalDate dataEfetivacao) {
        exigir(!estaConfirmada(), "Esta transacao ja foi confirmada");
        exigir(dataEfetivacao != null, "A data de efetivacao e obrigatoria para confirmar");

        return toBuilder()
                .status(StatusTransacao.CONFIRMADA)
                .dataEfetivacao(dataEfetivacao)
                .build();
    }

    public boolean ehParcelada() {
        return grupoParcelamentoId != null;
    }

    public boolean pertenceA(Long usuarioId) {
        return this.usuarioId.equals(usuarioId);
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new RegraDeNegocioException(mensagem);
        }
    }
}
