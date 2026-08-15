package com.clareza.application.usecase;

import com.clareza.application.port.in.ConsultarVisaoGeralUseCase;
import com.clareza.application.port.out.TransacaoRepositoryPort;
import com.clareza.domain.model.TotalMensal;
import com.clareza.domain.model.VisaoGeral;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultarVisaoGeral implements ConsultarVisaoGeralUseCase {

    static final int MESES_FUTUROS = 3;

    private final TransacaoRepositoryPort transacaoRepository;

    @Override
    @Transactional(readOnly = true)
    public VisaoGeral consultar(Long usuarioId) {
        YearMonth competenciaAtual = YearMonth.from(LocalDate.now());
        LocalDate limite = competenciaAtual.plusMonths(MESES_FUTUROS).atEndOfMonth();

        List<TotalMensal> totais = transacaoRepository.totalizarPorMesAte(usuarioId, limite);

        return VisaoGeral.montar(totais, competenciaAtual, MESES_FUTUROS);
    }
}
