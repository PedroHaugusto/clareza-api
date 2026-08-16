package com.clareza.application.usecase;

import com.clareza.application.port.in.ComandoDeRecorrencia;
import com.clareza.application.port.in.GerenciarRecorrenciasUseCase;
import com.clareza.application.port.out.TransacaoRecorrenteRepositoryPort;
import com.clareza.application.port.out.TransacaoRepositoryPort;
import com.clareza.domain.exception.RecursoNaoEncontradoException;
import com.clareza.domain.exception.RegraDeNegocioException;
import com.clareza.domain.model.Transacao;
import com.clareza.domain.model.TransacaoRecorrente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GerenciarRecorrencias implements GerenciarRecorrenciasUseCase {

    static final int HORIZONTE_EM_MESES = 12;

    private final TransacaoRecorrenteRepositoryPort recorrenteRepository;
    private final TransacaoRepositoryPort transacaoRepository;
    private final VinculosDaTransacao vinculos;
    private final Clock relogio;

    @Override
    @Transactional(readOnly = true)
    public List<TransacaoRecorrente> listar(Long usuarioId) {
        return recorrenteRepository.listarDoUsuario(usuarioId);
    }

    @Override
    @Transactional
    public TransacaoRecorrente criar(ComandoDeRecorrencia comando) {
        vinculos.exigirContaDoUsuario(comando.getContaId(), comando.getUsuarioId());
        vinculos.exigirCategoriaVisivel(comando.getCategoriaId(), comando.getUsuarioId());

        TransacaoRecorrente salva = recorrenteRepository.salvar(TransacaoRecorrente.builder()
                .usuarioId(comando.getUsuarioId())
                .contaId(comando.getContaId())
                .categoriaId(comando.getCategoriaId())
                .descricao(comando.getDescricao())
                .valor(comando.getValor())
                .tipo(comando.getTipo())
                .periodicidade(comando.getPeriodicidade())
                .diaDoMes(comando.getDiaDoMes())
                .diaDaSemana(comando.getDiaDaSemana())
                .dataInicio(comando.getDataInicio())
                .dataFim(comando.getDataFim())
                .build());

        materializarOcorrencias(salva);
        return salva;
    }

    @Override
    @Transactional
    public void desativar(Long recorrenteId, Long usuarioId) {
        TransacaoRecorrente recorrente = recorrenteRepository.buscarPorId(recorrenteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transacao recorrente", recorrenteId));

        if (!recorrente.pertenceA(usuarioId)) {
            throw new RecursoNaoEncontradoException("Transacao recorrente", recorrenteId);
        }
        if (!recorrente.isAtiva()) {
            throw new RegraDeNegocioException("Esta recorrencia ja esta desativada");
        }

        recorrenteRepository.salvar(recorrente.desativar());
        transacaoRepository.excluirFuturasNaoConfirmadasDaRecorrencia(recorrenteId, LocalDate.now(relogio));
    }

    private void materializarOcorrencias(TransacaoRecorrente recorrente) {
        LocalDate hoje = LocalDate.now(relogio);
        List<LocalDate> datas = recorrente.ocorrenciasEntre(hoje, hoje.plusMonths(HORIZONTE_EM_MESES));

        if (datas.isEmpty()) {
            return;
        }

        List<Transacao> ocorrencias = new ArrayList<>(datas.size());
        for (LocalDate data : datas) {
            ocorrencias.add(Transacao.builder()
                    .usuarioId(recorrente.getUsuarioId())
                    .contaId(recorrente.getContaId())
                    .categoriaId(recorrente.getCategoriaId())
                    .descricao(recorrente.getDescricao())
                    .valor(recorrente.getValor())
                    .tipo(recorrente.getTipo())
                    .dataPrevista(data)
                    .transacaoRecorrenteId(recorrente.getId())
                    .build());
        }

        transacaoRepository.salvarTodas(ocorrencias);
    }
}
