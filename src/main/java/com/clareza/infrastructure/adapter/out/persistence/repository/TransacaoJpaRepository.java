package com.clareza.infrastructure.adapter.out.persistence.repository;

import com.clareza.infrastructure.adapter.out.persistence.entity.TransacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransacaoJpaRepository extends JpaRepository<TransacaoEntity, Long> {

    List<TransacaoEntity> findByUsuarioIdOrderByDataPrevistaDescIdDesc(Long usuarioId);

    boolean existsByContaId(Long contaId);

    boolean existsByCategoriaId(Long categoriaId);
}
