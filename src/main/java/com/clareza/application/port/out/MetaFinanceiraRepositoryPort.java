package com.clareza.application.port.out;

import com.clareza.domain.model.MetaFinanceira;

import java.util.List;
import java.util.Optional;

public interface MetaFinanceiraRepositoryPort {

    List<MetaFinanceira> listarDoUsuario(Long usuarioId);

    Optional<MetaFinanceira> buscarPorId(Long id);

    MetaFinanceira salvar(MetaFinanceira meta);

    void excluir(Long id);
}
