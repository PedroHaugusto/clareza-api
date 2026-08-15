package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.domain.model.TipoTransacao;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class RequisicaoDeTransacao {

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
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataPrevista;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataEfetivacao;
}
