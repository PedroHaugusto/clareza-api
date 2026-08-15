package com.clareza.application.port.in;

import com.clareza.domain.model.Categoria;

import java.util.List;

public interface GerenciarCategoriasUseCase {

    List<Categoria> listar(Long usuarioId);

    Categoria criar(ComandoDeCriacaoDeCategoria comando);

    void excluir(Long categoriaId, Long usuarioId);
}
