package com.clareza.infrastructure.adapter.in.web;

import com.clareza.application.port.in.ComandoDeRecorrencia;
import com.clareza.application.port.in.GerenciarRecorrenciasUseCase;
import com.clareza.infrastructure.adapter.in.web.dto.RequisicaoDeRecorrencia;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaRecorrencia;
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
@RequestMapping("/api/transacoes-recorrentes")
@RequiredArgsConstructor
public class TransacaoRecorrenteController {

    private final GerenciarRecorrenciasUseCase gerenciarRecorrencias;

    @GetMapping
    public List<RespostaRecorrencia> listar(@AuthenticationPrincipal Long usuarioId) {
        return gerenciarRecorrencias.listar(usuarioId).stream()
                .map(RespostaRecorrencia::de)
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaRecorrencia criar(@AuthenticationPrincipal Long usuarioId,
                                     @Valid @RequestBody RequisicaoDeRecorrencia requisicao) {
        ComandoDeRecorrencia comando = ComandoDeRecorrencia.builder()
                .usuarioId(usuarioId)
                .contaId(requisicao.getContaId())
                .categoriaId(requisicao.getCategoriaId())
                .descricao(requisicao.getDescricao())
                .valor(requisicao.getValor())
                .tipo(requisicao.getTipo())
                .periodicidade(requisicao.getPeriodicidade())
                .diaDoMes(requisicao.getDiaDoMes())
                .diaDaSemana(requisicao.getDiaDaSemana())
                .dataInicio(requisicao.getDataInicio())
                .dataFim(requisicao.getDataFim())
                .build();

        return RespostaRecorrencia.de(gerenciarRecorrencias.criar(comando));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@AuthenticationPrincipal Long usuarioId, @PathVariable Long id) {
        gerenciarRecorrencias.desativar(id, usuarioId);
    }
}
