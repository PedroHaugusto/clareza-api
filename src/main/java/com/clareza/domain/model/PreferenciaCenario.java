package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode
public class PreferenciaCenario {

    public static final BigDecimal AJUSTE_PADRAO = BigDecimal.TEN;

    private static final BigDecimal MAXIMO = BigDecimal.valueOf(100);

    private final Long id;
    private final Long usuarioId;
    private final BigDecimal percentualAjusteReceita;
    private final BigDecimal percentualAjusteDespesa;

    @Builder(toBuilder = true)
    private PreferenciaCenario(Long id, Long usuarioId,
                               BigDecimal percentualAjusteReceita,
                               BigDecimal percentualAjusteDespesa) {
        if (usuarioId == null) {
            throw new RegraDeNegocioException("A preferencia precisa pertencer a um usuario");
        }
        this.id = id;
        this.usuarioId = usuarioId;
        this.percentualAjusteReceita = validar(percentualAjusteReceita, "receita");
        this.percentualAjusteDespesa = validar(percentualAjusteDespesa, "despesa");
    }

    public static PreferenciaCenario padraoPara(Long usuarioId) {
        return PreferenciaCenario.builder()
                .usuarioId(usuarioId)
                .percentualAjusteReceita(AJUSTE_PADRAO)
                .percentualAjusteDespesa(AJUSTE_PADRAO)
                .build();
    }

    private static BigDecimal validar(BigDecimal percentual, String campo) {
        BigDecimal valor = percentual == null ? AJUSTE_PADRAO : percentual;
        if (valor.compareTo(BigDecimal.ZERO) < 0 || valor.compareTo(MAXIMO) > 0) {
            throw new RegraDeNegocioException(
                    String.format("O ajuste de %s deve estar entre 0 e 100", campo));
        }
        return valor;
    }
}
