package com.clareza.infrastructure.adapter.out.persistence.mapper;

import com.clareza.domain.model.Conta;
import com.clareza.infrastructure.adapter.out.persistence.entity.ContaEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContaPersistenceMapper {

    Conta paraDominio(ContaEntity entidade);

    ContaEntity paraEntidade(Conta conta);

    List<Conta> paraDominio(List<ContaEntity> entidades);
}
