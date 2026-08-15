package com.clareza.infrastructure.adapter.in.web;

import com.clareza.application.port.in.ComandoDeTransacao;
import com.clareza.application.port.in.GerenciarTransacoesUseCase;
import com.clareza.infrastructure.adapter.in.web.dto.RequisicaoDeTransacao;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaTransacao;
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
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final GerenciarTransacoesUseCase gerenciarTransacoes;

    @GetMapping
    public List<RespostaTransacao> listar(@AuthenticationPrincipal Long usuarioId) {
        LocalDate hoje = LocalDate.now();
        return gerenciarTransacoes.listar(usuarioId).stream()
                .map(transacao -> RespostaTransacao.de(transacao, hoje))
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaTransacao criar(@AuthenticationPrincipal Long usuarioId,
                                   @Valid @RequestBody RequisicaoDeTransacao requisicao) {
        return RespostaTransacao.de(
                gerenciarTransacoes.criar(comando(usuarioId, requisicao)), LocalDate.now());
    }

    @PutMapping("/{id}")
    public RespostaTransacao editar(@AuthenticationPrincipal Long usuarioId,
                                    @PathVariable Long id,
                                    @Valid @RequestBody RequisicaoDeTransacao requisicao) {
        return RespostaTransacao.de(
                gerenciarTransacoes.editar(id, comando(usuarioId, requisicao)), LocalDate.now());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal Long usuarioId, @PathVariable Long id) {
        gerenciarTransacoes.excluir(id, usuarioId);
    }

    private ComandoDeTransacao comando(Long usuarioId, RequisicaoDeTransacao requisicao) {
        return ComandoDeTransacao.builder()
                .usuarioId(usuarioId)
                .contaId(requisicao.getContaId())
                .categoriaId(requisicao.getCategoriaId())
                .descricao(requisicao.getDescricao())
                .valor(requisicao.getValor())
                .tipo(requisicao.getTipo())
                .dataPrevista(requisicao.getDataPrevista())
                .dataEfetivacao(requisicao.getDataEfetivacao())
                .build();
    }
}
