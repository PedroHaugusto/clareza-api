package com.clareza.application.usecase;

import com.clareza.application.port.in.CriarContasPadraoUseCase;
import com.clareza.application.port.out.ContaRepositoryPort;
import com.clareza.domain.model.Conta;
import com.clareza.domain.model.TipoConta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CriarContasPadrao implements CriarContasPadraoUseCase {

    static final String CONTA_PRINCIPAL = "Conta principal";
    static final String CARTAO_PRINCIPAL = "Cartão principal";

    private final ContaRepositoryPort contaRepository;

    @Override
    @Transactional
    public void criarPara(Long usuarioId) {
        contaRepository.salvar(Conta.builder()
                .usuarioId(usuarioId)
                .nome(CONTA_PRINCIPAL)
                .tipo(TipoConta.CONTA_CORRENTE)
                .build());

        contaRepository.salvar(Conta.builder()
                .usuarioId(usuarioId)
                .nome(CARTAO_PRINCIPAL)
                .tipo(TipoConta.CARTAO_CREDITO)
                .build());
    }
}
