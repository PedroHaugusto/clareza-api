package com.clareza.infrastructure.adapter.in.web;

import com.clareza.application.port.in.ConsultarVencimentosUseCase;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaTransacao;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vencimentos")
@RequiredArgsConstructor
public class VencimentoController {

    private final ConsultarVencimentosUseCase consultarVencimentos;
    private final Clock relogio;

    @GetMapping
    public List<RespostaTransacao> consultar(@AuthenticationPrincipal Long usuarioId) {
        LocalDate hoje = LocalDate.now(relogio);
        return consultarVencimentos.consultar(usuarioId).stream()
                .map(transacao -> RespostaTransacao.de(transacao, hoje))
                .collect(Collectors.toList());
    }
}
