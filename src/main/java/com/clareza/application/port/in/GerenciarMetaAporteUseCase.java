package com.clareza.application.port.in;

import com.clareza.domain.model.MetaAporteMensal;

import java.math.BigDecimal;
import java.util.Optional;

public interface GerenciarMetaAporteUseCase {

    Optional<MetaAporteMensal> consultar(Long usuarioId);

    MetaAporteMensal definir(Long usuarioId, BigDecimal valor);

    void remover(Long usuarioId);
}
