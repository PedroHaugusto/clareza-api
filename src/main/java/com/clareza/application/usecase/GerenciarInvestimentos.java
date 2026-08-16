package com.clareza.application.usecase;

import com.clareza.application.port.in.ComandoDeInvestimento;
import com.clareza.application.port.in.GerenciarInvestimentosUseCase;
import com.clareza.application.port.out.InvestimentoRepositoryPort;
import com.clareza.domain.exception.RecursoNaoEncontradoException;
import com.clareza.domain.model.Carteira;
import com.clareza.domain.model.Investimento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GerenciarInvestimentos implements GerenciarInvestimentosUseCase {

    private final InvestimentoRepositoryPort investimentoRepository;

    @Override
    @Transactional(readOnly = true)
    public Carteira consultarCarteira(Long usuarioId) {
        return Carteira.de(investimentoRepository.listarDoUsuario(usuarioId));
    }

    @Override
    @Transactional
    public Investimento criar(ComandoDeInvestimento comando) {
        return investimentoRepository.salvar(montar(null, comando));
    }

    @Override
    @Transactional
    public Investimento editar(Long investimentoId, ComandoDeInvestimento comando) {
        Investimento existente = buscarDoUsuario(investimentoId, comando.getUsuarioId());
        return investimentoRepository.salvar(montar(existente.getId(), comando));
    }

    @Override
    @Transactional
    public void excluir(Long investimentoId, Long usuarioId) {
        Investimento investimento = buscarDoUsuario(investimentoId, usuarioId);
        investimentoRepository.excluir(investimento.getId());
    }

    private Investimento montar(Long id, ComandoDeInvestimento comando) {
        return Investimento.builder()
                .id(id)
                .usuarioId(comando.getUsuarioId())
                .nome(comando.getNome())
                .tipo(comando.getTipo())
                .valorInvestido(comando.getValorInvestido())
                .rentabilidadeInformada(comando.getRentabilidadeInformada())
                .build();
    }

    private Investimento buscarDoUsuario(Long investimentoId, Long usuarioId) {
        Investimento investimento = investimentoRepository.buscarPorId(investimentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Investimento", investimentoId));

        if (!investimento.pertenceA(usuarioId)) {
            throw new RecursoNaoEncontradoException("Investimento", investimentoId);
        }
        return investimento;
    }
}
