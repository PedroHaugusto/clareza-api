package com.clareza.application.port.out;

import com.clareza.domain.model.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepositoryPort {

    List<Categoria> listarVisiveisPara(Long usuarioId);

    Optional<Categoria> buscarPorId(Long id);

    Categoria salvar(Categoria categoria);

    void excluir(Long id);

    boolean existeComNomeVisivelPara(String nome, Long usuarioId);
}
