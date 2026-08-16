package com.clareza.infrastructure.adapter.out.persistence.repository;

import com.clareza.infrastructure.adapter.out.persistence.entity.MetaFinanceiraEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetaFinanceiraJpaRepository extends JpaRepository<MetaFinanceiraEntity, Long> {

    List<MetaFinanceiraEntity> findByUsuarioIdOrderByNome(Long usuarioId);
}
