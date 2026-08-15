package com.clareza.infrastructure.adapter.in.web.dto;

import com.clareza.domain.model.Categoria;
import com.clareza.domain.model.TipoCategoria;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RespostaCategoria {

    Long id;
    String nome;
    TipoCategoria tipo;
    String corHex;
    boolean padraoDoSistema;

    public static RespostaCategoria de(Categoria categoria) {
        return RespostaCategoria.builder()
                .id(categoria.getId())
                .nome(categoria.getNome())
                .tipo(categoria.getTipo())
                .corHex(categoria.getCorHex())
                .padraoDoSistema(categoria.ehPadraoDoSistema())
                .build();
    }
}
