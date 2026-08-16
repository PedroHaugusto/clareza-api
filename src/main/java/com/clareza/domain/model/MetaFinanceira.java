package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@EqualsAndHashCode
public class MetaFinanceira {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final Long id;
    private final Long usuarioId;
    private final String nome;
    private final BigDecimal valorAtual;
    private final BigDecimal valorObjetivo;
    private final LocalDate prazo;
    private final String descricao;

    @Builder(toBuilder = true)
    private MetaFinanceira(Long id, Long usuarioId, String nome, BigDecimal valorAtual,
                           BigDecimal valorObjetivo, LocalDate prazo, String descricao) {
        if (usuarioId == null) {
            throw new RegraDeNegocioException("A meta precisa pertencer a um usuario");
        }
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraDeNegocioException("O nome da meta e obrigatorio");
        }
        if (valorObjetivo == null || valorObjetivo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("O valor objetivo deve ser positivo");
        }

        BigDecimal atual = valorAtual == null ? BigDecimal.ZERO.setScale(2) : valorAtual;
        if (atual.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraDeNegocioException("O valor atual nao pode ser negativo");
        }

        this.id = id;
        this.usuarioId = usuarioId;
        this.nome = nome.trim();
        this.valorAtual = atual;
        this.valorObjetivo = valorObjetivo;
        this.prazo = prazo;
        this.descricao = descricao == null || descricao.trim().isEmpty() ? null : descricao.trim();
    }

    public BigDecimal getPercentualConcluido() {
        return valorAtual.multiply(CEM).divide(valorObjetivo, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getValorRestante() {
        BigDecimal restante = valorObjetivo.subtract(valorAtual);
        return restante.compareTo(BigDecimal.ZERO) < 0
                ? BigDecimal.ZERO.setScale(2)
                : restante;
    }

    public boolean estaConcluida() {
        return valorAtual.compareTo(valorObjetivo) >= 0;
    }

    public Long diasAte(LocalDate hoje) {
        return prazo == null ? null : ChronoUnit.DAYS.between(hoje, prazo);
    }

    public boolean prazoVencidoEm(LocalDate hoje) {
        return prazo != null && prazo.isBefore(hoje) && !estaConcluida();
    }

    public boolean pertenceA(Long usuarioId) {
        return this.usuarioId.equals(usuarioId);
    }
}
