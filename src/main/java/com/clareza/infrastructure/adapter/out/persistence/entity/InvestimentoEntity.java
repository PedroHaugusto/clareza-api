package com.clareza.infrastructure.adapter.out.persistence.entity;

import com.clareza.domain.model.TipoInvestimento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "investimento")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestimentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoInvestimento tipo;

    @Column(name = "valor_investido", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorInvestido;

    @Column(name = "rentabilidade_informada", nullable = false, precision = 7, scale = 2)
    private BigDecimal rentabilidadeInformada;
}
