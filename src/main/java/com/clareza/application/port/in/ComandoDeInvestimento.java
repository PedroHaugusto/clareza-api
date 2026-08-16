package com.clareza.application.port.in;

import com.clareza.domain.model.TipoInvestimento;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ComandoDeInvestimento {

    Long usuarioId;
    String nome;
    TipoInvestimento tipo;
    BigDecimal valorInvestido;
    BigDecimal rentabilidadeInformada;
}
