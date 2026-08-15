package com.clareza.application.port.in;

import lombok.Value;

@Value
public class ComandoDeRegistro {

    String nome;
    String email;
    String senha;
}
