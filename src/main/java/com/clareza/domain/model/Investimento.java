package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode
public class Investimento {

    private static final BigDecimal RENTABILIDADE_MAXIMA = BigDecimal.valueOf(1000);

    private final Long id;
    private final Long usuarioId;
    private final String nome;
    private final TipoInvestimento tipo;
    private final BigDecimal valorInvestido;
    private final BigDecimal rentabilidadeInformada;

    @Builder(toBuilder = true)
    private Investimento(Long id, Long usuarioId, String nome, TipoInvestimento tipo,
                         BigDecimal valorInvestido, BigDecimal rentabilidadeInformada) {
        if (usuarioId == null) {
            throw new RegraDeNegocioException("O investimento precisa pertencer a um usuario");
        }
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraDeNegocioException("O nome do investimento e obrigatorio");
        }
        if (tipo == null) {
            throw new RegraDeNegocioException("O tipo do investimento e obrigatorio");
        }
        if (valorInvestido == null || valorInvestido.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("O valor investido deve ser positivo");
        }

        BigDecimal rentabilidade = rentabilidadeInformada == null
                ? BigDecimal.ZERO.setScale(2, java.math.RoundingMode.UNNECESSARY)
                : rentabilidadeInformada;
        if (rentabilidade.abs().compareTo(RENTABILIDADE_MAXIMA) > 0) {
            throw new RegraDeNegocioException("A rentabilidade informada deve estar entre -1000 e 1000");
        }

        this.id = id;
        this.usuarioId = usuarioId;
        this.nome = nome.trim();
        this.tipo = tipo;
        this.valorInvestido = valorInvestido;
        this.rentabilidadeInformada = rentabilidade;
    }

    public boolean pertenceA(Long usuarioId) {
        return this.usuarioId.equals(usuarioId);
    }
}
