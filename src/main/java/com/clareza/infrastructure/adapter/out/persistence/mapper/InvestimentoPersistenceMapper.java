package com.clareza.infrastructure.adapter.out.persistence.mapper;

import com.clareza.domain.model.Investimento;
import com.clareza.infrastructure.adapter.out.persistence.entity.InvestimentoEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvestimentoPersistenceMapper {

    Investimento paraDominio(InvestimentoEntity entidade);

    InvestimentoEntity paraEntidade(Investimento investimento);

    List<Investimento> paraDominio(List<InvestimentoEntity> entidades);
}
