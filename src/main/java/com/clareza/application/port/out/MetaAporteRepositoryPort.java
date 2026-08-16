package com.clareza.application.port.out;

import com.clareza.domain.model.MetaAporteMensal;

import java.util.Optional;

public interface MetaAporteRepositoryPort {

    Optional<MetaAporteMensal> buscarDoUsuario(Long usuarioId);

    MetaAporteMensal salvar(MetaAporteMensal meta);

    void excluirDoUsuario(Long usuarioId);
}
