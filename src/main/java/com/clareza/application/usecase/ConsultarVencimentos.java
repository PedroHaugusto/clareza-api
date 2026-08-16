package com.clareza.application.usecase;

import com.clareza.application.port.in.ConsultarVencimentosUseCase;
import com.clareza.application.port.out.TransacaoRepositoryPort;
import com.clareza.domain.model.Transacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultarVencimentos implements ConsultarVencimentosUseCase {

    static final int JANELA_EM_DIAS = 14;

    private final TransacaoRepositoryPort transacaoRepository;
    private final Clock relogio;

    @Override
    @Transactional(readOnly = true)
    public List<Transacao> consultar(Long usuarioId) {
        return transacaoRepository.listarPrevistasAte(
                usuarioId, LocalDate.now(relogio).plusDays(JANELA_EM_DIAS));
    }
}
