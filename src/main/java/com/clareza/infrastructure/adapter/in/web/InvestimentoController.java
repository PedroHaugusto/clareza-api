package com.clareza.infrastructure.adapter.in.web;

import com.clareza.application.port.in.ComandoDeInvestimento;
import com.clareza.application.port.in.GerenciarInvestimentosUseCase;
import com.clareza.infrastructure.adapter.in.web.dto.RequisicaoDeInvestimento;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaCarteira;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaInvestimento;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/investimentos")
@RequiredArgsConstructor
public class InvestimentoController {

    private final GerenciarInvestimentosUseCase gerenciarInvestimentos;

    @GetMapping
    public RespostaCarteira listar(@AuthenticationPrincipal Long usuarioId) {
        return RespostaCarteira.de(gerenciarInvestimentos.consultarCarteira(usuarioId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaInvestimento criar(@AuthenticationPrincipal Long usuarioId,
                                      @Valid @RequestBody RequisicaoDeInvestimento requisicao) {
        return RespostaInvestimento.de(gerenciarInvestimentos.criar(comando(usuarioId, requisicao)));
    }

    @PutMapping("/{id}")
    public RespostaInvestimento editar(@AuthenticationPrincipal Long usuarioId,
                                       @PathVariable Long id,
                                       @Valid @RequestBody RequisicaoDeInvestimento requisicao) {
        return RespostaInvestimento.de(
                gerenciarInvestimentos.editar(id, comando(usuarioId, requisicao)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal Long usuarioId, @PathVariable Long id) {
        gerenciarInvestimentos.excluir(id, usuarioId);
    }

    private ComandoDeInvestimento comando(Long usuarioId, RequisicaoDeInvestimento requisicao) {
        return ComandoDeInvestimento.builder()
                .usuarioId(usuarioId)
                .nome(requisicao.getNome())
                .tipo(requisicao.getTipo())
                .valorInvestido(requisicao.getValorInvestido())
                .rentabilidadeInformada(requisicao.getRentabilidadeInformada())
                .build();
    }
}
