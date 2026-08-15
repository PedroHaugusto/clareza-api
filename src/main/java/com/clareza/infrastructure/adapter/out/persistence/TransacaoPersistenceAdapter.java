package com.clareza.infrastructure.adapter.out.persistence;

import com.clareza.application.port.out.TransacaoRepositoryPort;
import com.clareza.domain.model.Transacao;
import com.clareza.infrastructure.adapter.out.persistence.mapper.TransacaoPersistenceMapper;
import com.clareza.infrastructure.adapter.out.persistence.repository.TransacaoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransacaoPersistenceAdapter implements TransacaoRepositoryPort {

    private final TransacaoJpaRepository repository;
    private final TransacaoPersistenceMapper mapper;

    @Override
    public List<Transacao> listarDoUsuario(Long usuarioId) {
        return mapper.paraDominio(repository.findByUsuarioIdOrderByDataPrevistaDescIdDesc(usuarioId));
    }

    @Override
    public Optional<Transacao> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::paraDominio);
    }

    @Override
    public Transacao salvar(Transacao transacao) {
        return mapper.paraDominio(repository.save(mapper.paraEntidade(transacao)));
    }

    @Override
    public void excluir(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existeComConta(Long contaId) {
        return repository.existsByContaId(contaId);
    }

    @Override
    public boolean existeComCategoria(Long categoriaId) {
        return repository.existsByCategoriaId(categoriaId);
    }
}
