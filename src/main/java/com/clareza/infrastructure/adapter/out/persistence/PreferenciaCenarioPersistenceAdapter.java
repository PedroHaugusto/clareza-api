package com.clareza.infrastructure.adapter.out.persistence;

import com.clareza.application.port.out.PreferenciaCenarioRepositoryPort;
import com.clareza.domain.model.PreferenciaCenario;
import com.clareza.infrastructure.adapter.out.persistence.mapper.PreferenciaCenarioPersistenceMapper;
import com.clareza.infrastructure.adapter.out.persistence.repository.PreferenciaCenarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PreferenciaCenarioPersistenceAdapter implements PreferenciaCenarioRepositoryPort {

    private final PreferenciaCenarioJpaRepository repository;
    private final PreferenciaCenarioPersistenceMapper mapper;

    @Override
    public Optional<PreferenciaCenario> buscarDoUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId).map(mapper::paraDominio);
    }

    @Override
    public PreferenciaCenario salvar(PreferenciaCenario preferencia) {
        return mapper.paraDominio(repository.save(mapper.paraEntidade(preferencia)));
    }
}
