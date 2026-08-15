package com.clareza.application.port.in;

import com.clareza.domain.model.TipoTransacao;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class ComandoDeParcelamento {

    Long usuarioId;
    Long contaId;
    Long categoriaId;
    String descricao;
    BigDecimal valorTotal;
    TipoTransacao tipo;
    LocalDate dataDaPrimeiraParcela;
    int totalParcelas;
}
