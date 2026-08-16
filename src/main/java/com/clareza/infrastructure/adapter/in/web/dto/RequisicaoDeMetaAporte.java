package com.clareza.infrastructure.adapter.in.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class RequisicaoDeMetaAporte {

    @NotNull(message = "e obrigatorio")
    @DecimalMin(value = "0.01", message = "deve ser maior que zero")
    @Digits(integer = 13, fraction = 2, message = "aceita no maximo duas casas decimais")
    private BigDecimal valor;
}
