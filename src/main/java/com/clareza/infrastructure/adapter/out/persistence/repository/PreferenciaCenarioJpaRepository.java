package com.clareza.infrastructure.adapter.out.persistence.repository;

import com.clareza.infrastructure.adapter.out.persistence.entity.PreferenciaCenarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreferenciaCenarioJpaRepository extends JpaRepository<PreferenciaCenarioEntity, Long> {

    Optional<PreferenciaCenarioEntity> findByUsuarioId(Long usuarioId);
}
