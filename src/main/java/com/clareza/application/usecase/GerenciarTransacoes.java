package com.clareza.application.usecase;

import com.clareza.application.port.in.ComandoDeTransacao;
import com.clareza.application.port.in.FiltroDeTransacoes;
import com.clareza.application.port.in.GerenciarTransacoesUseCase;
import com.clareza.application.port.out.TransacaoRepositoryPort;
import com.clareza.domain.exception.RecursoNaoEncontradoException;
import com.clareza.domain.model.StatusTransacao;
import com.clareza.domain.model.Transacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GerenciarTransacoes implements GerenciarTransacoesUseCase {

    private final TransacaoRepositoryPort transacaoRepository;
    private final VinculosDaTransacao vinculos;

    @Override
    @Transactional(readOnly = true)
    public List<Transacao> listar(FiltroDeTransacoes filtro) {
        return transacaoRepository.listarComFiltro(
                filtro, filtro.getPeriodo().intervaloA(LocalDate.now()));
    }

    @Override
    @Transactional
    public Transacao confirmar(Long transacaoId, Long usuarioId) {
        Transacao transacao = buscarDoUsuario(transacaoId, usuarioId);
        return transacaoRepository.salvar(transacao.confirmarEm(LocalDate.now()));
    }

    @Override
    @Transactional
    public Transacao criar(ComandoDeTransacao comando) {
        exigirContaDoUsuario(comando.getContaId(), comando.getUsuarioId());
        exigirCategoriaVisivel(comando.getCategoriaId(), comando.getUsuarioId());

        return transacaoRepository.salvar(montar(null, comando));
    }

    @Override
    @Transactional
    public Transacao editar(Long transacaoId, ComandoDeTransacao comando) {
        Transacao existente = buscarDoUsuario(transacaoId, comando.getUsuarioId());
        exigirContaDoUsuario(comando.getContaId(), comando.getUsuarioId());
        exigirCategoriaVisivel(comando.getCategoriaId(), comando.getUsuarioId());

        return transacaoRepository.salvar(montar(existente.getId(), comando).toBuilder()
                .transacaoRecorrenteId(existente.getTransacaoRecorrenteId())
                .grupoParcelamentoId(existente.getGrupoParcelamentoId())
                .numeroParcela(existente.getNumeroParcela())
                .totalParcelas(existente.getTotalParcelas())
                .build());
    }

    @Override
    @Transactional
    public void excluir(Long transacaoId, Long usuarioId) {
        Transacao transacao = buscarDoUsuario(transacaoId, usuarioId);
        transacaoRepository.excluir(transacao.getId());
    }

    private Transacao montar(Long id, ComandoDeTransacao comando) {
        StatusTransacao status = comando.getDataEfetivacao() == null
                ? StatusTransacao.PREVISTA
                : StatusTransacao.CONFIRMADA;

        return Transacao.builder()
                .id(id)
                .usuarioId(comando.getUsuarioId())
                .contaId(comando.getContaId())
                .categoriaId(comando.getCategoriaId())
                .descricao(comando.getDescricao())
                .valor(comando.getValor())
                .tipo(comando.getTipo())
                .dataPrevista(comando.getDataPrevista())
                .dataEfetivacao(comando.getDataEfetivacao())
                .status(status)
                .build();
    }

    private Transacao buscarDoUsuario(Long transacaoId, Long usuarioId) {
        Transacao transacao = transacaoRepository.buscarPorId(transacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transacao", transacaoId));

        if (!transacao.pertenceA(usuarioId)) {
            throw new RecursoNaoEncontradoException("Transacao", transacaoId);
        }
        return transacao;
    }

    private void exigirContaDoUsuario(Long contaId, Long usuarioId) {
        vinculos.exigirContaDoUsuario(contaId, usuarioId);
    }

    private void exigirCategoriaVisivel(Long categoriaId, Long usuarioId) {
        vinculos.exigirCategoriaVisivel(categoriaId, usuarioId);
    }
}
