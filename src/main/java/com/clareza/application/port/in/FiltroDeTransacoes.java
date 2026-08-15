package com.clareza.application.port.in;

import com.clareza.domain.model.PeriodoDeBusca;
import com.clareza.domain.model.TipoTransacao;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FiltroDeTransacoes {

    Long usuarioId;
    TipoTransacao tipo;
    PeriodoDeBusca periodo;
    Long categoriaId;
    Long contaId;
    String busca;

    public PeriodoDeBusca getPeriodo() {
        return periodo == null ? PeriodoDeBusca.TODOS : periodo;
    }
}
