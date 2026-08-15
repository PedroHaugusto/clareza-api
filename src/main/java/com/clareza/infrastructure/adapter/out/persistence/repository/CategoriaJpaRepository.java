package com.clareza.infrastructure.adapter.out.persistence.repository;

import com.clareza.infrastructure.adapter.out.persistence.entity.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoriaJpaRepository extends JpaRepository<CategoriaEntity, Long> {

    @Query("SELECT c FROM CategoriaEntity c "
            + "WHERE c.usuarioId = :usuarioId OR c.usuarioId IS NULL "
            + "ORDER BY c.nome")
    List<CategoriaEntity> listarVisiveisPara(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COUNT(c) > 0 FROM CategoriaEntity c "
            + "WHERE LOWER(c.nome) = LOWER(:nome) "
            + "AND (c.usuarioId = :usuarioId OR c.usuarioId IS NULL)")
    boolean existeComNomeVisivelPara(@Param("nome") String nome, @Param("usuarioId") Long usuarioId);
}
