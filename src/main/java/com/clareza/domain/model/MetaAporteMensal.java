package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode
public class MetaAporteMensal {

    private final Long id;
    private final Long usuarioId;
    private final BigDecimal valor;

    @Builder(toBuilder = true)
    private MetaAporteMensal(Long id, Long usuarioId, BigDecimal valor) {
        if (usuarioId == null) {
            throw new RegraDeNegocioException("A meta precisa pertencer a um usuario");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("A meta de aporte deve ser positiva");
        }
        this.id = id;
        this.usuarioId = usuarioId;
        this.valor = valor;
    }
}
