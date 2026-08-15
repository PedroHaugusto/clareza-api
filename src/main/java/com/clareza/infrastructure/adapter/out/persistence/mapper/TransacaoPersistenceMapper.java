package com.clareza.infrastructure.adapter.out.persistence.mapper;

import com.clareza.domain.model.Transacao;
import com.clareza.infrastructure.adapter.out.persistence.entity.TransacaoEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransacaoPersistenceMapper {

    Transacao paraDominio(TransacaoEntity entidade);

    TransacaoEntity paraEntidade(Transacao transacao);

    List<Transacao> paraDominio(List<TransacaoEntity> entidades);
}
