package com.clareza.infrastructure.adapter.in.web.dto;

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
public class RequisicaoDeMetaFinanceira {

    @NotBlank(message = "nao pode ser vazio")
    @Size(max = 100, message = "deve ter no maximo 100 caracteres")
    private String nome;

    @DecimalMin(value = "0", message = "nao pode ser negativo")
    @Digits(integer = 13, fraction = 2, message = "aceita no maximo duas casas decimais")
    private BigDecimal valorAtual;

    @NotNull(message = "e obrigatorio")
    @DecimalMin(value = "0.01", message = "deve ser maior que zero")
    @Digits(integer = 13, fraction = 2, message = "aceita no maximo duas casas decimais")
    private BigDecimal valorObjetivo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate prazo;

    @Size(max = 255, message = "deve ter no maximo 255 caracteres")
    private String descricao;
}
