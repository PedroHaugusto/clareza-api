package com.clareza.infrastructure.adapter.in.web;

import com.clareza.application.port.in.ComandoDeCriacaoDeCategoria;
import com.clareza.application.port.in.GerenciarCategoriasUseCase;
import com.clareza.infrastructure.adapter.in.web.dto.RequisicaoDeCategoria;
import com.clareza.infrastructure.adapter.in.web.dto.RespostaCategoria;
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
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final GerenciarCategoriasUseCase gerenciarCategorias;

    @GetMapping
    public List<RespostaCategoria> listar(@AuthenticationPrincipal Long usuarioId) {
        return gerenciarCategorias.listar(usuarioId).stream()
                .map(RespostaCategoria::de)
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaCategoria criar(@AuthenticationPrincipal Long usuarioId,
                                   @Valid @RequestBody RequisicaoDeCategoria requisicao) {
        ComandoDeCriacaoDeCategoria comando = new ComandoDeCriacaoDeCategoria(
                usuarioId, requisicao.getNome(), requisicao.getTipo(), requisicao.getCorHex());
        return RespostaCategoria.de(gerenciarCategorias.criar(comando));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal Long usuarioId, @PathVariable Long id) {
        gerenciarCategorias.excluir(id, usuarioId);
    }
}
