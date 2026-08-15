package com.clareza.infrastructure.adapter.in.web;

import com.clareza.application.port.in.ConsultarVisaoGeralUseCase;
import com.clareza.domain.model.VisaoGeral;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaSaldo;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaVisaoGeral;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VisaoGeralController {

    private final ConsultarVisaoGeralUseCase consultarVisaoGeral;

    @GetMapping("/api/visao-geral")
    public RespostaVisaoGeral consultar(@AuthenticationPrincipal Long usuarioId) {
        return RespostaVisaoGeral.de(consultarVisaoGeral.consultar(usuarioId));
    }

    @GetMapping("/api/saldo-disponivel")
    public RespostaSaldo consultarSaldo(@AuthenticationPrincipal Long usuarioId) {
        VisaoGeral visaoGeral = consultarVisaoGeral.consultar(usuarioId);
        return new RespostaSaldo(visaoGeral.getSaldoDisponivel(), visaoGeral.getSaldoRealizado());
    }
}
