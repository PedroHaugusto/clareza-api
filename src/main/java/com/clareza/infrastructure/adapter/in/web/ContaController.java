package com.clareza.infrastructure.adapter.in.web;

import com.clareza.application.port.in.ComandoDeCriacaoDeConta;
import com.clareza.application.port.in.GerenciarContasUseCase;
import com.clareza.infrastructure.adapter.in.web.dto.RequisicaoDeConta;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaConta;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contas")
@RequiredArgsConstructor
public class ContaController {

    private final GerenciarContasUseCase gerenciarContas;

    @GetMapping
    public List<RespostaConta> listar(@AuthenticationPrincipal Long usuarioId) {
        return gerenciarContas.listar(usuarioId).stream()
                .map(RespostaConta::de)
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaConta criar(@AuthenticationPrincipal Long usuarioId,
                               @Valid @RequestBody RequisicaoDeConta requisicao) {
        ComandoDeCriacaoDeConta comando =
                new ComandoDeCriacaoDeConta(usuarioId, requisicao.getNome(), requisicao.getTipo());
        return RespostaConta.de(gerenciarContas.criar(comando));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal Long usuarioId, @PathVariable Long id) {
        gerenciarContas.excluir(id, usuarioId);
    }
}
