package com.clareza.application.usecase;

import com.clareza.application.port.in.ComandoDePrevisao;
import com.clareza.application.port.in.ConsultarPrevisaoUseCase;
import com.clareza.application.port.in.ConsultarVisaoGeralUseCase;
import com.clareza.application.port.in.GerenciarPreferenciaCenarioUseCase;
import com.clareza.application.port.out.TransacaoRepositoryPort;
import com.clareza.domain.model.PreferenciaCenario;
import com.clareza.domain.model.Previsao;
import com.clareza.domain.model.Transacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultarPrevisao implements ConsultarPrevisaoUseCase {

    private final TransacaoRepositoryPort transacaoRepository;
    private final GerenciarPreferenciaCenarioUseCase gerenciarPreferencia;
    private final ConsultarVisaoGeralUseCase consultarVisaoGeral;

    @Override
    @Transactional(readOnly = true)
    public Previsao consultar(ComandoDePrevisao comando) {
        YearMonth primeiraCompetencia = YearMonth.from(LocalDate.now()).plusMonths(1);
        YearMonth ultimaCompetencia = primeiraCompetencia.plusMonths(comando.getMeses() - 1L);

        List<Transacao> transacoes = transacaoRepository.listarPorIntervalo(
                comando.getUsuarioId(),
                primeiraCompetencia.atDay(1),
                ultimaCompetencia.atEndOfMonth());

        return Previsao.montar(
                primeiraCompetencia,
                comando.getMeses(),
                consultarVisaoGeral.consultar(comando.getUsuarioId()).getSaldoDisponivel(),
                transacoes,
                comando.getCenario(),
                resolverPreferencia(comando));
    }

    private PreferenciaCenario resolverPreferencia(ComandoDePrevisao comando) {
        if (comando.getAjusteReceita() == null && comando.getAjusteDespesa() == null) {
            return gerenciarPreferencia.consultar(comando.getUsuarioId());
        }

        PreferenciaCenario salva = gerenciarPreferencia.consultar(comando.getUsuarioId());
        return salva.toBuilder()
                .percentualAjusteReceita(comando.getAjusteReceita() == null
                        ? salva.getPercentualAjusteReceita()
                        : comando.getAjusteReceita())
                .percentualAjusteDespesa(comando.getAjusteDespesa() == null
                        ? salva.getPercentualAjusteDespesa()
                        : comando.getAjusteDespesa())
                .build();
    }
}
