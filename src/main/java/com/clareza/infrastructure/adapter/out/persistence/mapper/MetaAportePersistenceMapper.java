package com.clareza.infrastructure.adapter.out.persistence.mapper;

import com.clareza.domain.model.MetaAporteMensal;
import com.clareza.infrastructure.adapter.out.persistence.entity.MetaAporteMensalEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MetaAportePersistenceMapper {

    MetaAporteMensal paraDominio(MetaAporteMensalEntity entidade);

    MetaAporteMensalEntity paraEntidade(MetaAporteMensal meta);
}
