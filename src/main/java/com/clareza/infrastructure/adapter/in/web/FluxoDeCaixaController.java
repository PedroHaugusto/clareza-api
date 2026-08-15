package com.clareza.infrastructure.adapter.in.web;

import com.clareza.application.port.in.ConsultarFluxoDeCaixaUseCase;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaFluxoDeCaixa;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fluxo-caixa")
@RequiredArgsConstructor
public class FluxoDeCaixaController {

    private final ConsultarFluxoDeCaixaUseCase consultarFluxoDeCaixa;

    @GetMapping
    public RespostaFluxoDeCaixa consultar(@AuthenticationPrincipal Long usuarioId,
                                          @RequestParam(defaultValue = "6") int mesesPassados,
                                          @RequestParam(defaultValue = "6") int mesesFuturos) {
        return RespostaFluxoDeCaixa.de(
                consultarFluxoDeCaixa.consultar(usuarioId, mesesPassados, mesesFuturos));
    }
}
