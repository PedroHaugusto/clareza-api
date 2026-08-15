package com.clareza.infrastructure.adapter.out.persistence.entity;

import com.clareza.domain.model.StatusTransacao;
import com.clareza.domain.model.TipoTransacao;
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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transacao")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "conta_id", nullable = false)
    private Long contaId;

    @Column(name = "categoria_id", nullable = false)
    private Long categoriaId;

    @Column(nullable = false, length = 150)
    private String descricao;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoTransacao tipo;

    @Column(name = "data_prevista", nullable = false)
    private LocalDate dataPrevista;

    @Column(name = "data_efetivacao")
    private LocalDate dataEfetivacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusTransacao status;

    @Column(name = "transacao_recorrente_id")
    private Long transacaoRecorrenteId;

    @Column(name = "grupo_parcelamento_id")
    private UUID grupoParcelamentoId;

    @Column(name = "numero_parcela")
    private Integer numeroParcela;

    @Column(name = "total_parcelas")
    private Integer totalParcelas;
}
