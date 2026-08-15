package com.clareza.infrastructure.adapter.in.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class RequisicaoDePreferenciaCenario {

    @NotNull(message = "e obrigatorio")
    @DecimalMin(value = "0", message = "deve estar entre 0 e 100")
    @DecimalMax(value = "100", message = "deve estar entre 0 e 100")
    private BigDecimal percentualAjusteReceita;

    @NotNull(message = "e obrigatorio")
    @DecimalMin(value = "0", message = "deve estar entre 0 e 100")
    @DecimalMax(value = "100", message = "deve estar entre 0 e 100")
    private BigDecimal percentualAjusteDespesa;
}
