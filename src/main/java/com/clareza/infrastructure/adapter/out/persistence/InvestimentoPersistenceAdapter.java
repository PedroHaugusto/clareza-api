package com.clareza.infrastructure.adapter.out.persistence;

import com.clareza.application.port.out.InvestimentoRepositoryPort;
import com.clareza.domain.model.Investimento;
import com.clareza.infrastructure.adapter.out.persistence.mapper.InvestimentoPersistenceMapper;
import com.clareza.infrastructure.adapter.out.persistence.repository.InvestimentoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InvestimentoPersistenceAdapter implements InvestimentoRepositoryPort {

    private final InvestimentoJpaRepository repository;
    private final InvestimentoPersistenceMapper mapper;

    @Override
    public List<Investimento> listarDoUsuario(Long usuarioId) {
        return mapper.paraDominio(repository.findByUsuarioIdOrderByNome(usuarioId));
    }

    @Override
    public Optional<Investimento> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::paraDominio);
    }

    @Override
    public Investimento salvar(Investimento investimento) {
        return mapper.paraDominio(repository.save(mapper.paraEntidade(investimento)));
    }

    @Override
    public void excluir(Long id) {
        repository.deleteById(id);
    }
}
