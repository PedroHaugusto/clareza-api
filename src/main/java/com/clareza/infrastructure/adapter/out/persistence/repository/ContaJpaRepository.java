package com.clareza.infrastructure.adapter.out.persistence.repository;

import com.clareza.infrastructure.adapter.out.persistence.entity.ContaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContaJpaRepository extends JpaRepository<ContaEntity, Long> {

    List<ContaEntity> findByUsuarioIdOrderByNome(Long usuarioId);

    boolean existsByUsuarioIdAndNomeIgnoreCase(Long usuarioId, String nome);
}
