package com.clareza.application.port.in;

import com.clareza.domain.model.TipoConta;
import lombok.Value;

@Value
public class ComandoDeCriacaoDeConta {

    Long usuarioId;
    String nome;
    TipoConta tipo;
}
