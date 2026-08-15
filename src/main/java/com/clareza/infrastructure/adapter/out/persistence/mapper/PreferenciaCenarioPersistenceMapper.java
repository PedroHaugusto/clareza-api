package com.clareza.infrastructure.adapter.out.persistence.mapper;

import com.clareza.domain.model.PreferenciaCenario;
import com.clareza.infrastructure.adapter.out.persistence.entity.PreferenciaCenarioEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PreferenciaCenarioPersistenceMapper {

    PreferenciaCenario paraDominio(PreferenciaCenarioEntity entidade);

    PreferenciaCenarioEntity paraEntidade(PreferenciaCenario preferencia);
}
