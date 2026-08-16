package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.domain.model.TipoInvestimento;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class RequisicaoDeInvestimento {

    @NotBlank(message = "nao pode ser vazio")
    @Size(max = 100, message = "deve ter no maximo 100 caracteres")
    private String nome;

    @NotNull(message = "e obrigatorio")
    private TipoInvestimento tipo;

    @NotNull(message = "e obrigatorio")
    @DecimalMin(value = "0.01", message = "deve ser maior que zero")
    @Digits(integer = 13, fraction = 2, message = "aceita no maximo duas casas decimais")
    private BigDecimal valorInvestido;

    @DecimalMin(value = "-1000", message = "deve estar entre -1000 e 1000")
    @DecimalMax(value = "1000", message = "deve estar entre -1000 e 1000")
    @Digits(integer = 5, fraction = 2, message = "aceita no maximo duas casas decimais")
    private BigDecimal rentabilidadeInformada;
}
