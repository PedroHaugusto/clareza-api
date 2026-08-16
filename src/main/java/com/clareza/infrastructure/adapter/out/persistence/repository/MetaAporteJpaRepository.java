package com.clareza.infrastructure.adapter.out.persistence.repository;

import com.clareza.infrastructure.adapter.out.persistence.entity.MetaAporteMensalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetaAporteJpaRepository extends JpaRepository<MetaAporteMensalEntity, Long> {

    Optional<MetaAporteMensalEntity> findByUsuarioId(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);
}
