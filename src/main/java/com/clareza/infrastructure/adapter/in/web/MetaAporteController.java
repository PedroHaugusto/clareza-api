package com.clareza.infrastructure.adapter.in.web;

import com.clareza.application.port.in.GerenciarMetaAporteUseCase;
import com.clareza.infrastructure.adapter.in.web.dto.RequisicaoDeMetaAporte;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaMetaAporte;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/meta-aporte")
@RequiredArgsConstructor
public class MetaAporteController {

    private final GerenciarMetaAporteUseCase gerenciarMetaAporte;

    @GetMapping
    public RespostaMetaAporte consultar(@AuthenticationPrincipal Long usuarioId) {
        return RespostaMetaAporte.de(gerenciarMetaAporte.consultar(usuarioId));
    }

    @PutMapping
    public RespostaMetaAporte definir(@AuthenticationPrincipal Long usuarioId,
                                      @Valid @RequestBody RequisicaoDeMetaAporte requisicao) {
        return RespostaMetaAporte.de(
                Optional.of(gerenciarMetaAporte.definir(usuarioId, requisicao.getValor())));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@AuthenticationPrincipal Long usuarioId) {
        gerenciarMetaAporte.remover(usuarioId);
    }
}
