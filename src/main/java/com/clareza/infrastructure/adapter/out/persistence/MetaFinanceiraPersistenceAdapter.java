package com.clareza.infrastructure.adapter.out.persistence;

import com.clareza.application.port.out.MetaFinanceiraRepositoryPort;
import com.clareza.domain.model.MetaFinanceira;
import com.clareza.infrastructure.adapter.out.persistence.mapper.MetaFinanceiraPersistenceMapper;
import com.clareza.infrastructure.adapter.out.persistence.repository.MetaFinanceiraJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MetaFinanceiraPersistenceAdapter implements MetaFinanceiraRepositoryPort {

    private final MetaFinanceiraJpaRepository repository;
    private final MetaFinanceiraPersistenceMapper mapper;

    @Override
    public List<MetaFinanceira> listarDoUsuario(Long usuarioId) {
        return mapper.paraDominio(repository.findByUsuarioIdOrderByNome(usuarioId));
    }

    @Override
    public Optional<MetaFinanceira> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::paraDominio);
    }

    @Override
    public MetaFinanceira salvar(MetaFinanceira meta) {
        return mapper.paraDominio(repository.save(mapper.paraEntidade(meta)));
    }

    @Override
    public void excluir(Long id) {
        repository.deleteById(id);
    }
}
