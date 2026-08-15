package com.clareza.infrastructure.config;

import com.clareza.infrastructure.adapter.in.web.dto.RespostaErro;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class PontoDeEntradaNaoAutorizado implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest requisicao,
                         HttpServletResponse resposta,
                         AuthenticationException excecao) throws IOException {
        RespostaErro corpo = RespostaErro.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .erro(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .mensagem("Autenticacao necessaria para acessar este recurso")
                .path(requisicao.getRequestURI())
                .build();

        resposta.setStatus(HttpStatus.UNAUTHORIZED.value());
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(resposta.getOutputStream(), corpo);
    }
}