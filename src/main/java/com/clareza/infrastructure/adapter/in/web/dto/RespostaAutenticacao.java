package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.application.port.in.UsuarioAutenticado;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class RespostaAutenticacao {

    Long id;
    String nome;
    String email;
    String token;
    String tipo;
    Instant expiraEm;

    public static RespostaAutenticacao de(UsuarioAutenticado autenticado) {
        return RespostaAutenticacao.builder()
                .id(autenticado.getId())
                .nome(autenticado.getNome())
                .email(autenticado.getEmail())
                .token(autenticado.getToken())
                .tipo("Bearer")
                .expiraEm(autenticado.getExpiraEm())
                .build();
    }
}
