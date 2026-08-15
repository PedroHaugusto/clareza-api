package com.clareza.infrastructure.adapter.out.persistence;

import com.clareza.application.port.out.CategoriaRepositoryPort;
import com.clareza.domain.model.Categoria;
import com.clareza.infrastructure.adapter.out.persistence.mapper.CategoriaPersistenceMapper;
import com.clareza.infrastructure.adapter.out.persistence.repository.CategoriaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CategoriaPersistenceAdapter implements CategoriaRepositoryPort {

    private final CategoriaJpaRepository repository;
    private final CategoriaPersistenceMapper mapper;

    @Override
    public List<Categoria> listarVisiveisPara(Long usuarioId) {
        return mapper.paraDominio(repository.listarVisiveisPara(usuarioId));
    }

    @Override
    public Optional<Categoria> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::paraDominio);
    }

    @Override
    public Categoria salvar(Categoria categoria) {
        return mapper.paraDominio(repository.save(mapper.paraEntidade(categoria)));
    }

    @Override
    public void excluir(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existeComNomeVisivelPara(String nome, Long usuarioId) {
        return repository.existeComNomeVisivelPara(nome, usuarioId);
    }
}
