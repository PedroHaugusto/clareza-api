package com.clareza.application.port.in;

import com.clareza.domain.model.TipoCategoria;
import lombok.Value;

@Value
public class ComandoDeCriacaoDeCategoria {

    Long usuarioId;
    String nome;
    TipoCategoria tipo;
    String corHex;
}
