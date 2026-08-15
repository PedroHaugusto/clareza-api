package com.clareza.infrastructure.adapter.in.web;

import com.clareza.application.port.in.ComandoDePrevisao;
import com.clareza.application.port.in.ConsultarPrevisaoUseCase;
import com.clareza.application.port.in.GerenciarPreferenciaCenarioUseCase;
import com.clareza.domain.model.Cenario;
import com.clareza.infrastructure.adapter.in.web.dto.RequisicaoDePreferenciaCenario;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaPreferenciaCenario;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaPrevisao;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class PrevisaoController {

    private final ConsultarPrevisaoUseCase consultarPrevisao;
    private final GerenciarPreferenciaCenarioUseCase gerenciarPreferencia;

    @GetMapping("/api/previsao")
    public RespostaPrevisao consultar(@AuthenticationPrincipal Long usuarioId,
                                      @RequestParam(defaultValue = "6") int meses,
                                      @RequestParam(defaultValue = "PROVAVEL") Cenario cenario,
                                      @RequestParam(required = false) BigDecimal ajusteReceita,
                                      @RequestParam(required = false) BigDecimal ajusteDespesa) {
        ComandoDePrevisao comando = ComandoDePrevisao.builder()
                .usuarioId(usuarioId)
                .meses(meses)
                .cenario(cenario)
                .ajusteReceita(ajusteReceita)
                .ajusteDespesa(ajusteDespesa)
                .build();

        return RespostaPrevisao.de(consultarPrevisao.consultar(comando), LocalDate.now());
    }

    @GetMapping("/api/preferencia-cenario")
    public RespostaPreferenciaCenario consultarPreferencia(@AuthenticationPrincipal Long usuarioId) {
        return RespostaPreferenciaCenario.de(gerenciarPreferencia.consultar(usuarioId));
    }

    @PutMapping("/api/preferencia-cenario")
    public RespostaPreferenciaCenario salvarPreferencia(
            @AuthenticationPrincipal Long usuarioId,
            @Valid @RequestBody RequisicaoDePreferenciaCenario requisicao) {
        return RespostaPreferenciaCenario.de(gerenciarPreferencia.salvar(
                usuarioId,
                requisicao.getPercentualAjusteReceita(),
                requisicao.getPercentualAjusteDespesa()));
    }
}
