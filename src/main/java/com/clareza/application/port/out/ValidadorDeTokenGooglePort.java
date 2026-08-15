package com.clareza.application.port.out;

import lombok.Value;

public interface ValidadorDeTokenGooglePort {

    ContaGoogle validar(String idToken);

    @Value
    class ContaGoogle {
        String googleId;
        String email;
        String nome;
    }
}
