package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.domain.model.Conta;
import com.clareza.domain.model.TipoConta;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RespostaConta {

    Long id;
    String nome;
    TipoConta tipo;
    boolean cartaoDeCredito;

    public static RespostaConta de(Conta conta) {
        return RespostaConta.builder()
                .id(conta.getId())
                .nome(conta.getNome())
                .tipo(conta.getTipo())
                .cartaoDeCredito(conta.ehCartaoDeCredito())
                .build();
    }
}
