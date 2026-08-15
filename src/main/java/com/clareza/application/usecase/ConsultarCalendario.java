package com.clareza.application.usecase;

import com.clareza.application.port.in.ConsultarCalendarioUseCase;
import com.clareza.application.port.out.TransacaoRepositoryPort;
import com.clareza.domain.exception.RegraDeNegocioException;
import com.clareza.domain.model.CalendarioMensal;
import com.clareza.domain.model.Transacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultarCalendario implements ConsultarCalendarioUseCase {

    private final TransacaoRepositoryPort transacaoRepository;

    @Override
    @Transactional(readOnly = true)
    public CalendarioMensal consultar(Long usuarioId, int mes, int ano) {
        if (mes < 1 || mes > 12) {
            throw new RegraDeNegocioException("O mes deve estar entre 1 e 12");
        }

        YearMonth referencia = YearMonth.of(ano, mes);
        List<Transacao> transacoes = transacaoRepository.listarPorIntervalo(
                usuarioId, referencia.atDay(1), referencia.atEndOfMonth());

        return CalendarioMensal.montar(mes, ano, transacoes);
    }
}
