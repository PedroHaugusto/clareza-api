package com.clareza.application.port.out;

import com.clareza.domain.model.Usuario;
import lombok.Value;

import java.time.Instant;

public interface GeradorDeTokenPort {

    TokenGerado gerarPara(Usuario usuario);

    @Value
    class TokenGerado {
        String token;
        Instant expiraEm;
    }
}
