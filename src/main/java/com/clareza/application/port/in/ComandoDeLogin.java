package com.clareza.application.port.in;

import lombok.Value;

@Value
public class ComandoDeLogin {

    String email;
    String senha;
}