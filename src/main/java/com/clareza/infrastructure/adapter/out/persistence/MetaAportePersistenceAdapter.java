package com.clareza.infrastructure.adapter.out.persistence;

import com.clareza.application.port.out.MetaAporteRepositoryPort;
import com.clareza.domain.model.MetaAporteMensal;
import com.clareza.infrastructure.adapter.out.persistence.mapper.MetaAportePersistenceMapper;
import com.clareza.infrastructure.adapter.out.persistence.repository.MetaAporteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MetaAportePersistenceAdapter implements MetaAporteRepositoryPort {

    private final MetaAporteJpaRepository repository;
    private final MetaAportePersistenceMapper mapper;

    @Override
    public Optional<MetaAporteMensal> buscarDoUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId).map(mapper::paraDominio);
    }

    @Override
    public MetaAporteMensal salvar(MetaAporteMensal meta) {
        return mapper.paraDominio(repository.save(mapper.paraEntidade(meta)));
    }

    @Override
    public void excluirDoUsuario(Long usuarioId) {
        repository.deleteByUsuarioId(usuarioId);
    }
}
