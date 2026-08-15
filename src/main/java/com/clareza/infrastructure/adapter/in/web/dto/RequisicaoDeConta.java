package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.domain.model.TipoConta;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class RequisicaoDeConta {

    @NotBlank(message = "nao pode ser vazio")
    @Size(max = 60, message = "deve ter no maximo 60 caracteres")
    private String nome;

    @NotNull(message = "e obrigatorio")
    private TipoConta tipo;
}
