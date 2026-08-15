package com.clareza.infrastructure.adapter.in.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class RequisicaoDeRegistro {

    @NotBlank(message = "nao pode ser vazio")
    @Size(max = 120, message = "deve ter no maximo 120 caracteres")
    private String nome;

    @NotBlank(message = "nao pode ser vazio")
    @Email(message = "deve ser um e-mail valido")
    @Size(max = 180, message = "deve ter no maximo 180 caracteres")
    private String email;

    @NotBlank(message = "nao pode ser vazio")
    @Size(min = 8, max = 72, message = "deve ter entre 8 e 72 caracteres")
    private String senha;
}
