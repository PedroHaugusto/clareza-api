package com.clareza.infrastructure.adapter.out.persistence.repository;

import com.clareza.infrastructure.adapter.out.persistence.entity.TransacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TransacaoJpaRepository
        extends JpaRepository<TransacaoEntity, Long>, JpaSpecificationExecutor<TransacaoEntity> {

    List<TransacaoEntity> findByUsuarioIdOrderByDataPrevistaDescIdDesc(Long usuarioId);

    boolean existsByContaId(Long contaId);

    boolean existsByCategoriaId(Long categoriaId);
}
