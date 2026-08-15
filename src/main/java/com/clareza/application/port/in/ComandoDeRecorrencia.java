package com.clareza.application.port.in;

import com.clareza.domain.model.Periodicidade;
import com.clareza.domain.model.TipoTransacao;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class ComandoDeRecorrencia {

    Long usuarioId;
    Long contaId;
    Long categoriaId;
    String descricao;
    BigDecimal valor;
    TipoTransacao tipo;
    Periodicidade periodicidade;
    Integer diaDoMes;
    Integer diaDaSemana;
    LocalDate dataInicio;
    LocalDate dataFim;
}
