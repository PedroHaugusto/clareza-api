package com.clareza.application.usecase;

import com.clareza.application.port.in.ComandoDeCriacaoDeConta;
import com.clareza.application.port.in.GerenciarContasUseCase;
import com.clareza.application.port.out.ContaRepositoryPort;
import com.clareza.domain.exception.RecursoNaoEncontradoException;
import com.clareza.domain.exception.RegraDeNegocioException;
import com.clareza.domain.model.Conta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GerenciarContas implements GerenciarContasUseCase {

    private final ContaRepositoryPort contaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Conta> listar(Long usuarioId) {
        return contaRepository.listarDoUsuario(usuarioId);
    }

    @Override
    @Transactional
    public Conta criar(ComandoDeCriacaoDeConta comando) {
        if (contaRepository.existeComNomeDoUsuario(comando.getNome(), comando.getUsuarioId())) {
            throw new RegraDeNegocioException("Ja existe uma conta com este nome");
        }

        return contaRepository.salvar(Conta.builder()
                .usuarioId(comando.getUsuarioId())
                .nome(comando.getNome())
                .tipo(comando.getTipo())
                .build());
    }

    @Override
    @Transactional
    public void excluir(Long contaId, Long usuarioId) {
        Conta conta = contaRepository.buscarPorId(contaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta", contaId));

        if (!conta.pertenceA(usuarioId)) {
            throw new RecursoNaoEncontradoException("Conta", contaId);
        }

        contaRepository.excluir(contaId);
    }
}
