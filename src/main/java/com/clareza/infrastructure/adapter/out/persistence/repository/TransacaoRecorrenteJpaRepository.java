package com.clareza.infrastructure.adapter.out.persistence.repository;

import com.clareza.infrastructure.adapter.out.persistence.entity.TransacaoRecorrenteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransacaoRecorrenteJpaRepository extends JpaRepository<TransacaoRecorrenteEntity, Long> {

    List<TransacaoRecorrenteEntity> findByUsuarioIdOrderByDescricao(Long usuarioId);
}
