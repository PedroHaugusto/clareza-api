package com.clareza.infrastructure.adapter.in.web;

import com.clareza.application.port.in.ConsultarCalendarioUseCase;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaCalendario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/calendario")
@RequiredArgsConstructor
public class CalendarioController {

    private final ConsultarCalendarioUseCase consultarCalendario;
    private final Clock relogio;

    @GetMapping
    public RespostaCalendario consultar(@AuthenticationPrincipal Long usuarioId,
                                        @RequestParam(required = false) Integer mes,
                                        @RequestParam(required = false) Integer ano) {
        LocalDate hoje = LocalDate.now(relogio);
        int mesConsultado = mes == null ? hoje.getMonthValue() : mes;
        int anoConsultado = ano == null ? hoje.getYear() : ano;

        return RespostaCalendario.de(
                consultarCalendario.consultar(usuarioId, mesConsultado, anoConsultado), hoje);
    }
}
