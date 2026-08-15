package com.clareza.infrastructure.adapter.in.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class RequisicaoDeLoginComGoogle {

    @NotBlank(message = "nao pode ser vazio")
    private String idToken;
}
