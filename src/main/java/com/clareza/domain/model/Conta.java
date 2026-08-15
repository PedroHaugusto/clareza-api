package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Conta {

    private final Long id;
    private final Long usuarioId;
    private final String nome;
    private final TipoConta tipo;

    @Builder(toBuilder = true)
    private Conta(Long id, Long usuarioId, String nome, TipoConta tipo) {
        if (usuarioId == null) {
            throw new RegraDeNegocioException("A conta precisa pertencer a um usuario");
        }
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraDeNegocioException("O nome da conta e obrigatorio");
        }
        if (tipo == null) {
            throw new RegraDeNegocioException("O tipo da conta e obrigatorio");
        }
        this.id = id;
        this.usuarioId = usuarioId;
        this.nome = nome.trim();
        this.tipo = tipo;
    }

    public boolean ehCartaoDeCredito() {
        return TipoConta.CARTAO_CREDITO == tipo;
    }

    public boolean pertenceA(Long usuarioId) {
        return this.usuarioId.equals(usuarioId);
    }
}
