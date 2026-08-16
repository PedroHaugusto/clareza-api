package com.clareza.infrastructure.adapter.in.web;

import com.clareza.application.port.in.ComandoDeMetaFinanceira;
import com.clareza.application.port.in.GerenciarMetasFinanceirasUseCase;
import com.clareza.infrastructure.adapter.in.web.dto.RequisicaoDeMetaFinanceira;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaMetaFinanceira;
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
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/metas")
@RequiredArgsConstructor
public class MetaFinanceiraController {

    private final GerenciarMetasFinanceirasUseCase gerenciarMetas;
    private final Clock relogio;

    @GetMapping
    public List<RespostaMetaFinanceira> listar(@AuthenticationPrincipal Long usuarioId) {
        LocalDate hoje = LocalDate.now(relogio);
        return gerenciarMetas.listar(usuarioId).stream()
                .map(meta -> RespostaMetaFinanceira.de(meta, hoje))
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaMetaFinanceira criar(@AuthenticationPrincipal Long usuarioId,
                                        @Valid @RequestBody RequisicaoDeMetaFinanceira requisicao) {
        return RespostaMetaFinanceira.de(
                gerenciarMetas.criar(comando(usuarioId, requisicao)), LocalDate.now(relogio));
    }

    @PutMapping("/{id}")
    public RespostaMetaFinanceira editar(@AuthenticationPrincipal Long usuarioId,
                                         @PathVariable Long id,
                                         @Valid @RequestBody RequisicaoDeMetaFinanceira requisicao) {
        return RespostaMetaFinanceira.de(
                gerenciarMetas.editar(id, comando(usuarioId, requisicao)), LocalDate.now(relogio));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal Long usuarioId, @PathVariable Long id) {
        gerenciarMetas.excluir(id, usuarioId);
    }

    private ComandoDeMetaFinanceira comando(Long usuarioId, RequisicaoDeMetaFinanceira requisicao) {
        return ComandoDeMetaFinanceira.builder()
                .usuarioId(usuarioId)
                .nome(requisicao.getNome())
                .valorAtual(requisicao.getValorAtual())
                .valorObjetivo(requisicao.getValorObjetivo())
                .prazo(requisicao.getPrazo())
                .descricao(requisicao.getDescricao())
                .build();
    }
}
