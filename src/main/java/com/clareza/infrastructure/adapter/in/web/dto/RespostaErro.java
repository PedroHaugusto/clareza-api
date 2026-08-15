package com.clareza.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class RespostaErro {

    Instant timestamp;
    int status;
    String erro;
    String mensagem;
    String path;
    List<CampoInvalido> campos;

    @Value
    public static class CampoInvalido {
        String campo;
        String mensagem;
    }
}