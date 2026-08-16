package com.clareza.application.port.in;

import com.clareza.domain.model.MetaFinanceira;

import java.util.List;

public interface GerenciarMetasFinanceirasUseCase {

    List<MetaFinanceira> listar(Long usuarioId);

    MetaFinanceira criar(ComandoDeMetaFinanceira comando);

    MetaFinanceira editar(Long metaId, ComandoDeMetaFinanceira comando);

    void excluir(Long metaId, Long usuarioId);
}
