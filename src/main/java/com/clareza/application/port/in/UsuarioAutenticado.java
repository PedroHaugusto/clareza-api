package com.clareza.application.port.in;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class UsuarioAutenticado {

    Long id;
    String nome;
    String email;
    String token;
    Instant expiraEm;
}