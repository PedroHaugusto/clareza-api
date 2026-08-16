package com.clareza.infrastructure.adapter.out.persistence.mapper;

import com.clareza.domain.model.MetaFinanceira;
import com.clareza.infrastructure.adapter.out.persistence.entity.MetaFinanceiraEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MetaFinanceiraPersistenceMapper {

    MetaFinanceira paraDominio(MetaFinanceiraEntity entidade);

    MetaFinanceiraEntity paraEntidade(MetaFinanceira meta);

    List<MetaFinanceira> paraDominio(List<MetaFinanceiraEntity> entidades);
}
