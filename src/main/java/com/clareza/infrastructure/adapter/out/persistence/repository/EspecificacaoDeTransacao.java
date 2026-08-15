package com.clareza.infrastructure.adapter.out.persistence.repository;

import com.clareza.application.port.in.FiltroDeTransacoes;
import com.clareza.domain.model.IntervaloDeDatas;
import com.clareza.infrastructure.adapter.out.persistence.entity.TransacaoEntity;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class EspecificacaoDeTransacao {

    private EspecificacaoDeTransacao() {
    }

    public static Specification<TransacaoEntity> de(FiltroDeTransacoes filtro, IntervaloDeDatas intervalo) {
        return (raiz, consulta, construtor) -> {
            List<Predicate> predicados = new ArrayList<>();

            predicados.add(construtor.equal(raiz.get("usuarioId"), filtro.getUsuarioId()));

            if (filtro.getTipo() != null) {
                predicados.add(construtor.equal(raiz.get("tipo"), filtro.getTipo()));
            }
            if (filtro.getCategoriaId() != null) {
                predicados.add(construtor.equal(raiz.get("categoriaId"), filtro.getCategoriaId()));
            }
            if (filtro.getContaId() != null) {
                predicados.add(construtor.equal(raiz.get("contaId"), filtro.getContaId()));
            }
            if (intervalo != null) {
                predicados.add(construtor.between(
                        raiz.get("dataPrevista"), intervalo.getInicio(), intervalo.getFim()));
            }
            if (filtro.getBusca() != null && !filtro.getBusca().trim().isEmpty()) {
                String termo = "%" + filtro.getBusca().trim().toLowerCase(Locale.ROOT) + "%";
                predicados.add(construtor.like(construtor.lower(raiz.get("descricao")), termo));
            }

            return construtor.and(predicados.toArray(new Predicate[0]));
        };
    }
}
