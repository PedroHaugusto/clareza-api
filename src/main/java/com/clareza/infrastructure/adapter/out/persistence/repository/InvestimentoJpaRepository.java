package com.clareza.infrastructure.adapter.out.persistence.repository;

import com.clareza.infrastructure.adapter.out.persistence.entity.InvestimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestimentoJpaRepository extends JpaRepository<InvestimentoEntity, Long> {

    List<InvestimentoEntity> findByUsuarioIdOrderByNome(Long usuarioId);
}
