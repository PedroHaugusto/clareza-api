package com.clareza.domain.model;

import com.clareza.domain.exception.RegraDeNegocioException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Locale;
import java.util.regex.Pattern;

@Getter
@EqualsAndHashCode
public class Categoria {

    private static final Pattern COR_HEXADECIMAL = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    private final Long id;
    private final Long usuarioId;
    private final String nome;
    private final TipoCategoria tipo;
    private final String corHex;

    @Builder(toBuilder = true)
    private Categoria(Long id, Long usuarioId, String nome, TipoCategoria tipo, String corHex) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraDeNegocioException("O nome da categoria e obrigatorio");
        }
        if (tipo == null) {
            throw new RegraDeNegocioException("O tipo da categoria e obrigatorio");
        }
        this.id = id;
        this.usuarioId = usuarioId;
        this.nome = nome.trim();
        this.tipo = tipo;
        this.corHex = normalizarCor(corHex);
    }

    public boolean ehPadraoDoSistema() {
        return usuarioId == null;
    }

    public boolean pertenceA(Long usuarioId) {
        return this.usuarioId != null && this.usuarioId.equals(usuarioId);
    }

    private static String normalizarCor(String corHex) {
        if (corHex == null || corHex.trim().isEmpty()) {
            throw new RegraDeNegocioException("A cor da categoria e obrigatoria");
        }
        String cor = corHex.trim();
        if (!COR_HEXADECIMAL.matcher(cor).matches()) {
            throw new RegraDeNegocioException("A cor deve estar no formato #RRGGBB");
        }
        return cor.toUpperCase(Locale.ROOT);
    }
}
