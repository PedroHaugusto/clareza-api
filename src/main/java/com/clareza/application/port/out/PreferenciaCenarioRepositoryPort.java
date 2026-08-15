package com.clareza.application.port.out;

import com.clareza.domain.model.PreferenciaCenario;

import java.util.Optional;

public interface PreferenciaCenarioRepositoryPort {

    Optional<PreferenciaCenario> buscarDoUsuario(Long usuarioId);

    PreferenciaCenario salvar(PreferenciaCenario preferencia);
}
