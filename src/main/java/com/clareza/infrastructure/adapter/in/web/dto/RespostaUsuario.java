package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.domain.model.Usuario;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RespostaUsuario {

    Long id;
    String nome;
    String email;
    boolean possuiSenha;
    boolean vinculadoAoGoogle;

    public static RespostaUsuario de(Usuario usuario) {
        return RespostaUsuario.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .possuiSenha(usuario.possuiSenha())
                .vinculadoAoGoogle(usuario.vinculadoAoGoogle())
                .build();
    }
}
