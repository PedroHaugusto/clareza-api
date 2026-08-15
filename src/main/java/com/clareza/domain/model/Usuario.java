package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Locale;

@Getter
@EqualsAndHashCode
public class Usuario {

    private final Long id;
    private final String nome;
    private final String email;
    private final String senhaHash;
    private final String googleId;

    @Builder(toBuilder = true)
    private Usuario(Long id, String nome, String email, String senhaHash, String googleId) {
        if (senhaHash == null && googleId == null) {
            throw new RegraDeNegocioException("Usuario precisa de uma senha ou de um vinculo com o Google");
        }
        this.id = id;
        this.nome = exigirPreenchido(nome, "nome");
        this.email = normalizarEmail(email);
        this.senhaHash = senhaHash;
        this.googleId = googleId;
    }

    public boolean possuiSenha() {
        return senhaHash != null;
    }

    public boolean vinculadoAoGoogle() {
        return googleId != null;
    }

    public Usuario vincularGoogle(String googleId) {
        if (googleId == null || googleId.trim().isEmpty()) {
            throw new RegraDeNegocioException("O identificador do Google e obrigatorio para vincular a conta");
        }
        return toBuilder().googleId(googleId).build();
    }

    private static String exigirPreenchido(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new RegraDeNegocioException(String.format("O campo %s e obrigatorio", campo));
        }
        return valor.trim();
    }

    private static String normalizarEmail(String email) {
        return exigirPreenchido(email, "email").toLowerCase(Locale.ROOT);
    }
}