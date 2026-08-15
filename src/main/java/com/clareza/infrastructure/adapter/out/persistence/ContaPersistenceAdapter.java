package com.clareza.infrastructure.adapter.out.persistence;

import com.clareza.application.port.out.ContaRepositoryPort;
import com.clareza.domain.model.Conta;
import com.clareza.infrastructure.adapter.out.persistence.mapper.ContaPersistenceMapper;
import com.clareza.infrastructure.adapter.out.persistence.repository.ContaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ContaPersistenceAdapter implements ContaRepositoryPort {

    private final ContaJpaRepository repository;
    private final ContaPersistenceMapper mapper;

    @Override
    public List<Conta> listarDoUsuario(Long usuarioId) {
        return mapper.paraDominio(repository.findByUsuarioIdOrderByNome(usuarioId));
    }

    @Override
    public Optional<Conta> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::paraDominio);
    }

    @Override
    public Conta salvar(Conta conta) {
        return mapper.paraDominio(repository.save(mapper.paraEntidade(conta)));
    }

    @Override
    public void excluir(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existeComNomeDoUsuario(String nome, Long usuarioId) {
        return repository.existsByUsuarioIdAndNomeIgnoreCase(usuarioId, nome);
    }
}
