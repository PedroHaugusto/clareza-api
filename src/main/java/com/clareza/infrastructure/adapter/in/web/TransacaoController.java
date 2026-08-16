package com.clareza.infrastructure.adapter.in.web;

import com.clareza.application.port.in.ComandoDeParcelamento;
import com.clareza.application.port.in.ComandoDeTransacao;
import com.clareza.application.port.in.CriarTransacaoParceladaUseCase;
import com.clareza.application.port.in.FiltroDeTransacoes;
import com.clareza.application.port.in.GerenciarTransacoesUseCase;
import com.clareza.domain.model.PeriodoDeBusca;
import com.clareza.domain.model.TipoTransacao;
import com.clareza.infrastructure.adapter.in.web.dto.RequisicaoDeParcelamento;
import com.clareza.infrastructure.adapter.in.web.dto.RequisicaoDeTransacao;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaTransacao;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final GerenciarTransacoesUseCase gerenciarTransacoes;
    private final CriarTransacaoParceladaUseCase criarTransacaoParcelada;
    private final Clock relogio;

    @GetMapping
    public List<RespostaTransacao> listar(@AuthenticationPrincipal Long usuarioId,
                                          @RequestParam(required = false) TipoTransacao tipo,
                                          @RequestParam(required = false) PeriodoDeBusca periodo,
                                          @RequestParam(required = false) Long categoriaId,
                                          @RequestParam(required = false) Long contaId,
                                          @RequestParam(required = false) String busca) {
        FiltroDeTransacoes filtro = FiltroDeTransacoes.builder()
                .usuarioId(usuarioId)
                .tipo(tipo)
                .periodo(periodo)
                .categoriaId(categoriaId)
                .contaId(contaId)
                .busca(busca)
                .build();

        LocalDate hoje = LocalDate.now(relogio);
        return gerenciarTransacoes.listar(filtro).stream()
                .map(transacao -> RespostaTransacao.de(transacao, hoje))
                .collect(Collectors.toList());
    }

    @PatchMapping("/{id}/confirmar")
    public RespostaTransacao confirmar(@AuthenticationPrincipal Long usuarioId, @PathVariable Long id) {
        return RespostaTransacao.de(gerenciarTransacoes.confirmar(id, usuarioId), LocalDate.now(relogio));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaTransacao criar(@AuthenticationPrincipal Long usuarioId,
                                   @Valid @RequestBody RequisicaoDeTransacao requisicao) {
        return RespostaTransacao.de(
                gerenciarTransacoes.criar(comando(usuarioId, requisicao)), LocalDate.now(relogio));
    }

    @PostMapping("/parcelada")
    @ResponseStatus(HttpStatus.CREATED)
    public List<RespostaTransacao> parcelar(@AuthenticationPrincipal Long usuarioId,
                                            @Valid @RequestBody RequisicaoDeParcelamento requisicao) {
        ComandoDeParcelamento comando = ComandoDeParcelamento.builder()
                .usuarioId(usuarioId)
                .contaId(requisicao.getContaId())
                .categoriaId(requisicao.getCategoriaId())
                .descricao(requisicao.getDescricao())
                .valorTotal(requisicao.getValorTotal())
                .tipo(requisicao.getTipo())
                .dataDaPrimeiraParcela(requisicao.getDataDaPrimeiraParcela())
                .totalParcelas(requisicao.getTotalParcelas())
                .build();

        LocalDate hoje = LocalDate.now(relogio);
        return criarTransacaoParcelada.parcelar(comando).stream()
                .map(transacao -> RespostaTransacao.de(transacao, hoje))
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public RespostaTransacao editar(@AuthenticationPrincipal Long usuarioId,
                                    @PathVariable Long id,
                                    @Valid @RequestBody RequisicaoDeTransacao requisicao) {
        return RespostaTransacao.de(
                gerenciarTransacoes.editar(id, comando(usuarioId, requisicao)), LocalDate.now(relogio));
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
