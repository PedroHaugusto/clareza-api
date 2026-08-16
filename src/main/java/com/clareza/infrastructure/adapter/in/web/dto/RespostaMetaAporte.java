package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.domain.model.MetaAporteMensal;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Optional;

@Value
@JsonInclude(JsonInclude.Include.ALWAYS)
public class RespostaMetaAporte {

    BigDecimal valor;
    boolean definida;

    public static RespostaMetaAporte de(Optional<MetaAporteMensal> meta) {
        return meta.map(existente -> new RespostaMetaAporte(existente.getValor(), true))
                .orElseGet(() -> new RespostaMetaAporte(null, false));
    }
}
