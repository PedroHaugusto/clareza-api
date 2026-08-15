package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.domain.model.Periodicidade;
import com.clareza.domain.model.TipoTransacao;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class RequisicaoDeRecorrencia {

    @NotNull(message = "e obrigatoria")
    private Long contaId;

    @NotNull(message = "e obrigatoria")
    private Long categoriaId;

    @NotBlank(message = "nao pode ser vazia")
    @Size(max = 150, message = "deve ter no maximo 150 caracteres")
    private String descricao;

    @NotNull(message = "e obrigatorio")
    @DecimalMin(value = "0.01", message = "deve ser maior que zero")
    @Digits(integer = 13, fraction = 2, message = "aceita no maximo duas casas decimais")
    private BigDecimal valor;

    @NotNull(message = "e obrigatorio")
    private TipoTransacao tipo;

    @NotNull(message = "e obrigatoria")
    private Periodicidade periodicidade;

    @Min(value = 1, message = "vai de 1 a 31")
    @Max(value = 31, message = "vai de 1 a 31")
    private Integer diaDoMes;

    @Min(value = 1, message = "vai de 1 a 7, sendo 1 segunda-feira")
    @Max(value = 7, message = "vai de 1 a 7, sendo 1 segunda-feira")
    private Integer diaDaSemana;

    @NotNull(message = "e obrigatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataInicio;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFim;
}
