package com.clareza.infrastructure.adapter.out.persistence;

import com.clareza.application.port.out.TransacaoRecorrenteRepositoryPort;
import com.clareza.domain.model.TransacaoRecorrente;
import com.clareza.infrastructure.adapter.out.persistence.mapper.TransacaoRecorrentePersistenceMapper;
import com.clareza.infrastructure.adapter.out.persistence.repository.TransacaoRecorrenteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransacaoRecorrentePersistenceAdapter implements TransacaoRecorrenteRepositoryPort {

    private final TransacaoRecorrenteJpaRepository repository;
    private final TransacaoRecorrentePersistenceMapper mapper;

    @Override
    public List<TransacaoRecorrente> listarDoUsuario(Long usuarioId) {
        return mapper.paraDominio(repository.findByUsuarioIdOrderByDescricao(usuarioId));
    }

    @Override
    public Optional<TransacaoRecorrente> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::paraDominio);
    }

    @Override
    public TransacaoRecorrente salvar(TransacaoRecorrente recorrente) {
        return mapper.paraDominio(repository.save(mapper.paraEntidade(recorrente)));
    }
}
