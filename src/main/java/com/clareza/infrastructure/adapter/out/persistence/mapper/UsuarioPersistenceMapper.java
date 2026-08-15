package com.clareza.infrastructure.adapter.out.persistence.mapper;

import com.clareza.domain.model.Usuario;
import com.clareza.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioPersistenceMapper {

    Usuario paraDominio(UsuarioEntity entidade);

    UsuarioEntity paraEntidade(Usuario usuario);
}