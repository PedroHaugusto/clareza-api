package com.clareza.infrastructure.adapter.out.persistence;

import com.clareza.application.port.in.FiltroDeTransacoes;
import com.clareza.application.port.out.TransacaoRepositoryPort;
import com.clareza.domain.model.IntervaloDeDatas;
import com.clareza.domain.model.Transacao;
import com.clareza.infrastructure.adapter.out.persistence.entity.TransacaoEntity;
import com.clareza.infrastructure.adapter.out.persistence.mapper.TransacaoPersistenceMapper;
import com.clareza.infrastructure.adapter.out.persistence.repository.EspecificacaoDeTransacao;
import com.clareza.infrastructure.adapter.out.persistence.repository.TransacaoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public List<Transacao> listarComFiltro(FiltroDeTransacoes filtro, IntervaloDeDatas intervalo) {
        return mapper.paraDominio(repository.findAll(
                EspecificacaoDeTransacao.de(filtro, intervalo),
                Sort.by(Sort.Direction.DESC, "dataPrevista", "id")));
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
    public List<Transacao> salvarTodas(List<Transacao> transacoes) {
        List<TransacaoEntity> entidades = transacoes.stream()
                .map(mapper::paraEntidade)
                .collect(Collectors.toList());
        return mapper.paraDominio(repository.saveAll(entidades));
    }

    @Override
    public void excluir(Long id) {
        repository.deleteById(id);
    }

    @Override
    public int excluirFuturasNaoConfirmadasDaRecorrencia(Long transacaoRecorrenteId, LocalDate apartirDe) {
        return repository.excluirFuturasNaoConfirmadas(transacaoRecorrenteId, apartirDe);
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
