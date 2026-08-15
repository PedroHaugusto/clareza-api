package com.clareza.application.usecase;

import com.clareza.application.port.in.ComandoDeCriacaoDeCategoria;
import com.clareza.application.port.in.GerenciarCategoriasUseCase;
import com.clareza.application.port.out.CategoriaRepositoryPort;
import com.clareza.domain.exception.RecursoNaoEncontradoException;
import com.clareza.domain.exception.RegraDeNegocioException;
import com.clareza.domain.model.Categoria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GerenciarCategorias implements GerenciarCategoriasUseCase {

    private final CategoriaRepositoryPort categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listar(Long usuarioId) {
        return categoriaRepository.listarVisiveisPara(usuarioId);
    }

    @Override
    @Transactional
    public Categoria criar(ComandoDeCriacaoDeCategoria comando) {
        if (categoriaRepository.existeComNomeVisivelPara(comando.getNome(), comando.getUsuarioId())) {
            throw new RegraDeNegocioException("Ja existe uma categoria com este nome");
        }

        return categoriaRepository.salvar(Categoria.builder()
                .usuarioId(comando.getUsuarioId())
                .nome(comando.getNome())
                .tipo(comando.getTipo())
                .corHex(comando.getCorHex())
                .build());
    }

    @Override
    @Transactional
    public void excluir(Long categoriaId, Long usuarioId) {
        Categoria categoria = categoriaRepository.buscarPorId(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", categoriaId));

        if (categoria.ehPadraoDoSistema()) {
            throw new RegraDeNegocioException("Categoria padrao do sistema nao pode ser excluida");
        }

        if (!categoria.pertenceA(usuarioId)) {
            throw new RecursoNaoEncontradoException("Categoria", categoriaId);
        }

        categoriaRepository.excluir(categoriaId);
    }
}
