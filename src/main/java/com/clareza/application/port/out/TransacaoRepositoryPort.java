package com.clareza.application.port.out;

import com.clareza.application.port.in.FiltroDeTransacoes;
import com.clareza.domain.model.IntervaloDeDatas;
import com.clareza.domain.model.TotalMensal;
import com.clareza.domain.model.Transacao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransacaoRepositoryPort {

    List<Transacao> listarDoUsuario(Long usuarioId);

    List<Transacao> listarComFiltro(FiltroDeTransacoes filtro, IntervaloDeDatas intervalo);

    List<Transacao> listarPorIntervalo(Long usuarioId, LocalDate inicio, LocalDate fim);

    List<Transacao> listarPrevistasAte(Long usuarioId, LocalDate limite);

    List<TotalMensal> totalizarPorMesAte(Long usuarioId, LocalDate limite);

    Optional<Transacao> buscarPorId(Long id);

    Transacao salvar(Transacao transacao);

    List<Transacao> salvarTodas(List<Transacao> transacoes);

    void excluir(Long id);

    int excluirFuturasNaoConfirmadasDaRecorrencia(Long transacaoRecorrenteId, LocalDate apartirDe);

    boolean existeComConta(Long contaId);

    boolean existeComCategoria(Long categoriaId);
}
