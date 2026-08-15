package com.clareza.infrastructure.adapter.out.persistence.repository;

import com.clareza.domain.model.StatusTransacao;
import com.clareza.infrastructure.adapter.out.persistence.entity.TransacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransacaoJpaRepository
        extends JpaRepository<TransacaoEntity, Long>, JpaSpecificationExecutor<TransacaoEntity> {

    List<TransacaoEntity> findByUsuarioIdOrderByDataPrevistaDescIdDesc(Long usuarioId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TransacaoEntity t "
            + "WHERE t.transacaoRecorrenteId = :recorrenteId "
            + "AND t.dataPrevista >= :apartirDe "
            + "AND t.status = com.clareza.domain.model.StatusTransacao.PREVISTA")
    int excluirFuturasNaoConfirmadas(@Param("recorrenteId") Long recorrenteId,
                                     @Param("apartirDe") LocalDate apartirDe);

    List<TransacaoEntity> findByUsuarioIdAndDataPrevistaBetweenOrderByDataPrevistaAscIdAsc(
            Long usuarioId, LocalDate inicio, LocalDate fim);

    List<TransacaoEntity> findByUsuarioIdAndStatusAndDataPrevistaLessThanEqualOrderByDataPrevistaAscIdAsc(
            Long usuarioId, StatusTransacao status, LocalDate limite);

    boolean existsByContaId(Long contaId);

    boolean existsByCategoriaId(Long categoriaId);
}
