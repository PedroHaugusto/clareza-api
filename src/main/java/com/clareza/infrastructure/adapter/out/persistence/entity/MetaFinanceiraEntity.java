package com.clareza.infrastructure.adapter.out.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "meta_financeira")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaFinanceiraEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "valor_atual", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAtual;

    @Column(name = "valor_objetivo", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorObjetivo;

    @Column
    private LocalDate prazo;

    @Column(length = 255)
    private String descricao;
}
