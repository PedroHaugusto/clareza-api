package com.clareza.application.port.in;

import com.clareza.domain.model.Cenario;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ComandoDePrevisao {

    Long usuarioId;
    int meses;
    Cenario cenario;
    BigDecimal ajusteReceita;
    BigDecimal ajusteDespesa;
}
