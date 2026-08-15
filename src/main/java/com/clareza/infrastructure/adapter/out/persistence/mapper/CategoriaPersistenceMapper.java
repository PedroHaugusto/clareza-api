package com.clareza.infrastructure.adapter.out.persistence.mapper;

import com.clareza.domain.model.Categoria;
import com.clareza.infrastructure.adapter.out.persistence.entity.CategoriaEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoriaPersistenceMapper {

    Categoria paraDominio(CategoriaEntity entidade);

    CategoriaEntity paraEntidade(Categoria categoria);

    List<Categoria> paraDominio(List<CategoriaEntity> entidades);
}
