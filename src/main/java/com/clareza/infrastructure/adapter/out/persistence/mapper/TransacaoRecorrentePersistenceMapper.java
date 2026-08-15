package com.clareza.infrastructure.adapter.out.persistence.mapper;

import com.clareza.domain.model.TransacaoRecorrente;
import com.clareza.infrastructure.adapter.out.persistence.entity.TransacaoRecorrenteEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransacaoRecorrentePersistenceMapper {

    TransacaoRecorrente paraDominio(TransacaoRecorrenteEntity entidade);

    TransacaoRecorrenteEntity paraEntidade(TransacaoRecorrente recorrente);

    List<TransacaoRecorrente> paraDominio(List<TransacaoRecorrenteEntity> entidades);
}
