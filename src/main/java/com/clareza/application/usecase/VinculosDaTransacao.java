package com.clareza.application.usecase;

import com.clareza.application.port.out.CategoriaRepositoryPort;
import com.clareza.application.port.out.ContaRepositoryPort;
import com.clareza.domain.exception.RecursoNaoEncontradoException;
import com.clareza.domain.model.Categoria;
import com.clareza.domain.model.Conta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VinculosDaTransacao {

    private final ContaRepositoryPort contaRepository;
    private final CategoriaRepositoryPort categoriaRepository;

    public void exigirContaDoUsuario(Long contaId, Long usuarioId) {
        Conta conta = contaRepository.buscarPorId(contaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta", contaId));

        if (!conta.pertenceA(usuarioId)) {
            throw new RecursoNaoEncontradoException("Conta", contaId);
        }
    }

    public void exigirCategoriaVisivel(Long categoriaId, Long usuarioId) {
        Categoria categoria = categoriaRepository.buscarPorId(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", categoriaId));

        if (!categoria.ehPadraoDoSistema() && !categoria.pertenceA(usuarioId)) {
            throw new RecursoNaoEncontradoException("Categoria", categoriaId);
        }
    }
}
