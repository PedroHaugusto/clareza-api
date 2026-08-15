package com.clareza.application.usecase;

import com.clareza.application.port.in.GerenciarPreferenciaCenarioUseCase;
import com.clareza.application.port.out.PreferenciaCenarioRepositoryPort;
import com.clareza.domain.model.PreferenciaCenario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class GerenciarPreferenciaCenario implements GerenciarPreferenciaCenarioUseCase {

    private final PreferenciaCenarioRepositoryPort preferenciaRepository;

    @Override
    @Transactional(readOnly = true)
    public PreferenciaCenario consultar(Long usuarioId) {
        return preferenciaRepository.buscarDoUsuario(usuarioId)
                .orElseGet(() -> PreferenciaCenario.padraoPara(usuarioId));
    }

    @Override
    @Transactional
    public PreferenciaCenario salvar(Long usuarioId, BigDecimal ajusteReceita, BigDecimal ajusteDespesa) {
        PreferenciaCenario existente = preferenciaRepository.buscarDoUsuario(usuarioId)
                .orElseGet(() -> PreferenciaCenario.padraoPara(usuarioId));

        return preferenciaRepository.salvar(existente.toBuilder()
                .percentualAjusteReceita(ajusteReceita)
                .percentualAjusteDespesa(ajusteDespesa)
                .build());
    }
}
